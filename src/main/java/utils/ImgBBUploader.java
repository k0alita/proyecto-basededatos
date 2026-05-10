package utils;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Properties;

public class ImgBBUploader {

    private static final String API_KEY;

    static {
        Properties props = new Properties();
        try (InputStream input = ImgBBUploader.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) throw new RuntimeException("No se encontró config.properties");
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar la API key de ImgBB", e);
        }
        API_KEY = props.getProperty("imgbb.api.key");
    }

    public static String subirImagen(File archivo) throws Exception {
        byte[] fileContent = Files.readAllBytes(archivo.toPath());
        String base64Image = Base64.getEncoder().encodeToString(fileContent);
        String encodedImage = URLEncoder.encode(base64Image, StandardCharsets.UTF_8);

        String body = "image=" + encodedImage;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imgbb.com/1/upload?key=" + API_KEY))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String res = response.body();
        if (res.contains("\"url\":\"")) {
            int start = res.indexOf("\"url\":\"") + 7;
            int end = res.indexOf("\"", start);
            return res.substring(start, end).replace("\\/", "/");
        }
        return null;
    }
}