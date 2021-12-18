package cz.tefek.youtubetoolkit.descrambler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Descrambler
{
    private final Function<String, String> descrambler;

    private Descrambler(List<Function<String, String>> transformers)
    {
        this.descrambler = transformers.stream()
            .sequential()
            .reduce(Function.identity(), Function::andThen);
    }

    public String descramble(String signature)
    {
        return this.descrambler.apply(signature);
    }

    private static String downloadScript(String playerJSUrl) throws IOException, InterruptedException
    {
        System.out.printf("Downloading script config from: %s\n", playerJSUrl);

        var httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(HttpClient.Version.HTTP_2);
        httpClientBuilder.followRedirects(HttpClient.Redirect.ALWAYS);
        httpClientBuilder.connectTimeout(Duration.ofSeconds(20));
        var httpClient = httpClientBuilder.build();

        var playerScriptRequestBuilder = HttpRequest.newBuilder();
        playerScriptRequestBuilder.GET();
        playerScriptRequestBuilder.uri(URI.create(playerJSUrl));
        var playerScriptRequest = playerScriptRequestBuilder.build();

        var response = httpClient.send(playerScriptRequest, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private static String getDescramblerCode(String playerSource)
    {

        var descramblerFuncPattern = Pattern.compile("""
            function\\([a-zA-Z0-9]+\\) # Function signature
            \\{
            (
            [a-z]=[a-z]\\.split\\(""\\); # Split the string into characters
            [a-zA-Z0-9.,();$]+? # Descrambler code
            return[a-z0-9]\\.join\\(""\\) # Join the characters into a string
            )
            """, Pattern.COMMENTS);

        var descrablerFuncMatcher = descramblerFuncPattern.matcher(playerSource);

        if (!descrablerFuncMatcher.find())
            throw new RuntimeException("Did not find the descrambler config!");

        return descrablerFuncMatcher.group(1);
    }

    private static String getInstructionCode(String playerSource, String field)
    {
        System.out.println("Descramble function object: " + field);

        Pattern auxFunPattern = Pattern.compile("var"
                                                + Pattern.quote(field)
                                                + "=\\{"
                                                + "((?:" + "[a-zA-Z]+[a-zA-Z0-9]*:function\\(.+?\\)"
                                                + "\\{.+?};?" + ")+)"
                                                + "}");
        Matcher auxFunMatcher = auxFunPattern.matcher(playerSource);

        boolean auxFound = auxFunMatcher.find();

        if (!auxFound)
            throw new RuntimeException("Did not find descramble functions!");

        return auxFunMatcher.group(1);
    }

    private static HashMap<String, DescramblerOperation> getInstructions(String playerSource, String obj)
    {
        var instructionCode = getInstructionCode(playerSource, obj);

        var auxFunctionArray = instructionCode.split("},");

        var funcMap = new HashMap<String, DescramblerOperation>();

        for (var auxFunction : auxFunctionArray)
        {
            String[] auxPair = auxFunction.split(":", 2);
            var key = auxPair[0];
            var auxCode = auxPair[1];

            if (auxCode.contains("splice"))
                funcMap.put(key, DescramblerOperation.SPLICE);
            else if (auxCode.contains("reverse"))
                funcMap.put(key, DescramblerOperation.REVERSE);
            else if (auxCode.contains("%"))
                funcMap.put(key, DescramblerOperation.SWAP);
            else
                throw new RuntimeException("Unrecognized descrambler instruction: " + auxCode + "}");
        }

        return funcMap;
    }

    public static Descrambler from(String playerJSUrlString) throws IOException, InterruptedException
    {
        var playerSource = downloadScript(playerJSUrlString);

        var playerSourceNoWhitespace = playerSource.replaceAll("\\s+", "");

        var descramblerCode = getDescramblerCode(playerSourceNoWhitespace);
        var descramblerCodeArr = descramblerCode.split(";");

        var descramblerInstructionCode = Arrays.stream(descramblerCodeArr)
            .sequential()
            .skip(1)
            .limit(descramblerCodeArr.length - 2)
            .map(c -> c.split("[.(),]"))
            .toList();

        var descramblerInstructionObjects = descramblerInstructionCode.stream()
            .map(cArr -> cArr[0])
            .distinct()
            .collect(Collectors.toMap(Function.identity(), k -> getInstructions(playerSourceNoWhitespace, k)));

        var descramblerInstructions = descramblerInstructionCode.stream()
            .map(cArr -> getMapper(cArr, descramblerInstructionObjects))
            .toList();

        System.out.println("Descrambler config found.");

        return new Descrambler(descramblerInstructions);
    }

    private enum DescramblerOperation
    {
        SPLICE,
        REVERSE,
        SWAP
    }

    private static Function<String, String> getMapper(String[] cArr, Map<String, ? extends Map<String, DescramblerOperation>> descramblerInstructionObjects)
    {
        var iObj = descramblerInstructionObjects.get(cArr[0]);
        var fn = iObj.get(cArr[1]);
        var idx = Integer.parseInt(cArr[3]);

        return switch (fn) {
            case SPLICE -> (s -> splice0(s, idx));
            case REVERSE -> Descrambler::reverse;
            case SWAP -> (s -> swap0(s, idx));
        };
    }

    private static String splice0(String input, int i)
    {
        return input.substring(i);
    }

    private static String reverse(String input)
    {
        return new StringBuilder(input).reverse().toString();
    }

    private static String swap0(String input, int index)
    {
        char[] charAr = input.toCharArray();

        char temp = charAr[0];
        charAr[0] = charAr[index % input.length()];
        charAr[index % input.length()] = temp;

        return new String(charAr);
    }
}
