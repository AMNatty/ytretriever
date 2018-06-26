package cz.tefek.youtubetoolkit;

import java.util.List;

import cz.tefek.youtubetoolkit.DescramblerHelper.ProcStep;

public class Descrambler
{
    private List<DescramblerHelper.ProcStep> man;

    public Descrambler(List<DescramblerHelper.ProcStep> man)
    {
        this.man = man;

        System.out.println("================================================================================");
        System.out.println("Descrambler:");
        for (ProcStep procStep : man)
        {
            if (procStep.getStepFunc() == Step.REVERSE)
            {
                System.out.println(" " + procStep.getStepFunc().toString());
            }
            else
            {
                System.out.println(" " + procStep.getStepFunc().toString() + ": " + procStep.getIndex());
            }
        }
        System.out.println("================================================================================");
    }

    public String descramble(String input)
    {
        String result = input;

        for (ProcStep procStep : man)
        {
            if (procStep.getStepFunc() == Step.SPLICE)
            {
                result = splice0(result, procStep.getIndex());
            }

            if (procStep.getStepFunc() == Step.REVERSE)
            {
                result = reverse(result);
            }

            if (procStep.getStepFunc() == Step.SWAP)
            {
                result = swap0(result, procStep.getIndex());
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
