package cz.tefek.youtubetoolkit.descrambler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DescramblerHelper
{
    public static String urlDecode(String input)
    {
        String out = null;

        if (input != null)
        {
            try
            {
                out = URLDecoder.decode(input, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        return out;
    }

    public static String urlEncode(String input)
    {
        String out = null;

        if (input != null)
        {
            try
            {
                out = URLEncoder.encode(input, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }

        return out;
    }

    public static class ProcStep
    {
        public ProcStep(DescramlerStep step, int index)
        {
            this.step = step;
            this.index = index;
        }

        private DescramlerStep step;
        private int index;

        public DescramlerStep getStepFunc()
        {
            return this.step;
        }

        public int getIndex()
        {
            return this.index;
        }
    }

    static class UnprocessedStep
    {
        public UnprocessedStep(String unprocOP, int index)
        {
            this.unprocOP = unprocOP;
            this.index = index;
        }

        private String unprocOP;
        private int index;

        public String getUnprocessedOP()
        {
            return this.unprocOP;
        }

        public int getIndex()
        {
            return this.index;
        }
    }

    public static List<ProcStep> update(String playerJSUrlString)
    {
        List<UnprocessedStep> stepsUn = new ArrayList<>();

        String reverser = null;
        String splicer = null;
        String swapper = null;

        try
        {
            StringBuilder scriptStringBuilder = new StringBuilder();
            URL playerJSUrl = new URL(playerJSUrlString);
            Scanner jsScanner = new Scanner(playerJSUrl.openStream());

            while (jsScanner.hasNext())
            {
                scriptStringBuilder.append(jsScanner.nextLine());
            }

            String playerSourceCodeNoWhites = scriptStringBuilder.toString().replaceAll("\\s+", "");

            Pattern descramblerFuncPattern = Pattern.compile("function\\([a-zA-Z0-9]+\\)\\{(a=a\\.split\\(\"\"\\);.+?returna\\.join\\(\\\"\\\"\\))");
            Matcher descrablerFuncMatcher = descramblerFuncPattern.matcher(playerSourceCodeNoWhites);

            boolean found = descrablerFuncMatcher.find();

            if (found)
            {
                String descramblerSrc = descrablerFuncMatcher.group(1);
                String[] descramblerCalls = descramblerSrc.split(";");

                String cont = null;

                for (int i = 0; i < descramblerCalls.length; i++)
                {
                    String descramblerCall = descramblerCalls[i];

                    if (i == 1)
                    {
                        cont = descramblerCall.substring(0, descramblerCall.indexOf("."));
                    }

                    if (i != 0 && i != descramblerCalls.length - 1)
                    {
                        String scoped = descramblerCall.substring(descramblerCall.indexOf(".") + 1);
                        String signatureU = scoped.substring(0, scoped.indexOf("("));
                        String indexUnpr = scoped.substring(scoped.indexOf(",") + 1).substring(0, scoped.substring(scoped.indexOf(",") + 1).length() - 1);

                        stepsUn.add(new UnprocessedStep(signatureU, Integer.parseInt(indexUnpr)));
                    }
                }

                Pattern auxFunPattern = Pattern.compile("var" + cont + "=\\{(.+?)\\}\\}");
                Matcher auxFunMatcher = auxFunPattern.matcher(playerSourceCodeNoWhites);

                boolean auxFound = auxFunMatcher.find();

                if (auxFound)
                {
                    String auxFunctions = auxFunMatcher.group(1);

                    String[] auxFunctionArray = auxFunctions.split("\\},");

                    for (int i = 0; i < auxFunctionArray.length; i++)
                    {
                        String auxFunction = auxFunctionArray[i];

                        String[] auxPair = auxFunction.split(":");

                        if (auxPair[1].contains("splice"))
                        {
                            splicer = auxPair[0];
                        }

                        if (auxPair[1].contains("reverse"))
                        {
                            reverser = auxPair[0];
                        }

                        if (auxPair[1].contains("%"))
                        {
                            swapper = auxPair[0];
                        }
                    }
                }
            }

            jsScanner.close();

            List<ProcStep> stepsF = new ArrayList<>();

            for (UnprocessedStep unprocessedStep : stepsUn)
            {
                if (unprocessedStep.getUnprocessedOP().equals(swapper))
                {
                    stepsF.add(new ProcStep(DescramlerStep.SWAP, unprocessedStep.getIndex()));
                }
                else if (unprocessedStep.getUnprocessedOP().equals(splicer))
                {
                    stepsF.add(new ProcStep(DescramlerStep.SPLICE, unprocessedStep.getIndex()));
                }
                else if (unprocessedStep.getUnprocessedOP().equals(reverser))
                {
                    stepsF.add(new ProcStep(DescramlerStep.REVERSE, unprocessedStep.getIndex()));
                }
            }

            return stepsF;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
