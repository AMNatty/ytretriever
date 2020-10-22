package cz.tefek.youtubetoolkit.descrambler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DescramblerHelper
{
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

    public static List<ProcStep> update(String playerJSUrlString) throws IOException
    {
        List<UnprocessedStep> stepsUn = new ArrayList<>();

        String reverser = null;
        String splicer = null;
        String swapper = null;

        StringBuilder scriptStringBuilder = new StringBuilder();
        URL playerJSUrl = new URL(playerJSUrlString);

        System.out.printf("Downloading script config from: %s\n", playerJSUrlString);

        try (InputStream is = playerJSUrl.openStream())
        {
            BufferedReader jsReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            String line;

            while ((line = jsReader.readLine()) != null)
            {
                scriptStringBuilder.append(line);
            }
        }

        String scriptSourceCode = scriptStringBuilder.toString();

        String playerSourceCodeNoWhites = scriptSourceCode.replaceAll("\\s+", "");

        Pattern descramblerFuncPattern = Pattern.compile("function\\([a-zA-Z0-9]+\\)" + "\\{" + "(" + "a=a\\.split\\(\"\"\\);" + "[a-zA-Z0-9.,();$]+?" + "returna\\.join\\(\"\"\\)" + ")");
        Matcher descrablerFuncMatcher = descramblerFuncPattern.matcher(playerSourceCodeNoWhites);

        boolean found = descrablerFuncMatcher.find();

        if (found)
        {
            String descramblerSrc = descrablerFuncMatcher.group(1);
            String[] descramblerCalls = descramblerSrc.split(";");

            String field = null;

            for (int i = 1; i < descramblerCalls.length - 1; i++)
            {
                String descramblerCall = descramblerCalls[i];
                var call = descramblerCall.split("[.(),]");
                field = call[0];

                String funcName = call[1];
                String indexUnpr = call[3];

                stepsUn.add(new UnprocessedStep(funcName, Integer.parseInt(indexUnpr)));
            }

            if (field == null)
                throw new RuntimeException("Failed to detect the descramble function object.");

            System.out.println("Descramble function object: " + field);

            Pattern auxFunPattern = Pattern.compile("var"
                    + Pattern.quote(field)
                    + "=\\{"
                    + "((?:" + "[a-zA-Z]+[a-zA-Z0-9]*:function\\(.+?\\)"
                    + "\\{.+?};?" + ")+)"
                    + "}");
            Matcher auxFunMatcher = auxFunPattern.matcher(playerSourceCodeNoWhites);

            boolean auxFound = auxFunMatcher.find();

            if (auxFound)
            {
                String auxFunctions = auxFunMatcher.group(1);

                String[] auxFunctionArray = auxFunctions.split("},");

                for (var auxFunction : auxFunctionArray)
                {
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

                System.out.println("Descrambler config found.");
            }
            else
            {
                System.err.println("Did not find descramble functions!");
            }
        }
        else
        {
            System.out.println("Descrambler config NOT found.");
        }

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
}
