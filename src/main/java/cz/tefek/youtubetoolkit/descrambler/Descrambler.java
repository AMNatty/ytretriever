package cz.tefek.youtubetoolkit.descrambler;

import java.util.List;

import cz.tefek.youtubetoolkit.descrambler.DescramblerHelper.ProcStep;

public class Descrambler
{
    private List<DescramblerHelper.ProcStep> descrambleSteps;

    public Descrambler(List<DescramblerHelper.ProcStep> man)
    {
        this.descrambleSteps = man;
    }

    public String descramble(String input)
    {
        String result = input;

        for (ProcStep procStep : this.descrambleSteps)
        {
            switch (procStep.getStepFunc())
            {
                case SPLICE:
                    result = splice0(result, procStep.getIndex());
                    break;
                case REVERSE:
                    result = reverse(result);
                    break;
                case SWAP:
                    result = this.swap0(result, procStep.getIndex());
                    break;
            }
        }

        return result;
    }

    private static String splice0(String input, int i)
    {
        return input.substring(i);
    }

    private static String reverse(String input)
    {
        return new StringBuilder(input).reverse().toString();
    }

    private String swap0(String input, int index)
    {
        char[] charAr = input.toCharArray();

        char temp = charAr[0];
        charAr[0] = charAr[index % input.length()];
        charAr[index % input.length()] = temp;

        return new String(charAr);
    }
}
