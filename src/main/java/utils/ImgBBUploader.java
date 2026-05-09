package utils;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ImgBBUploader {
    // Esta es la clave de la api de imgbb
    private static final String API_KEY = "c2bca37366fe2046d2e98dc177cd41df";

    public static String subirImagen(File archivo) throws Exception {
        // 1. Leemos la imagen y la convertimos en texto (Base64) para enviarla por internet
        byte[] fileContent = Files.readAllBytes(archivo.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);
        String encodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

        // 2. Preparamos la carta (petición POST) para ImgBB
        String body = "image=" + encodedImage;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imgbb.com/1/upload?key=" + API_KEY))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // 3. Enviamos la carta y esperamos respuesta
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // 4. ImgBB nos devuelve un JSON. Extraemos la URL "a lo bruto" sin librerías extra
        String res = response.body();
        if (res.contains("\"url\":\"")) {
            int start = res.indexOf("\"url\":\"") + 7;
            int end = res.indexOf("\"", start);
            // ImgBB devuelve barras raras como \/, así que las limpiamos
            return res.substring(start, end).replace("\\/", "/");
        }
        return null;
    }
}
