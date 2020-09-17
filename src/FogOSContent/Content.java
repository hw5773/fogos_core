package FogOSContent;

import FlexID.FlexID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Content implements FogOSContentAPI {
    private String name;        // The name of the content
    private String path;        // The path of the content
    private int numOfSegments;  // The number of segments of the content
    private FlexID[] flexID;    // The array of the Flex IDs of the content (Content ID)
    private String hash;
    private boolean shared;     // The flag that indicates whether the content is shared (or not)

    public Content(String name, String path, boolean shared) throws IOException, NoSuchAlgorithmException {
        this.name = name;
        this.path = path;
        this.shared = shared;
        this.hash = generateHash(path);
    }

    public Content(String name, String path, boolean shared, String hash) {
        this.name = name;
        this.path = path;
        this.shared = shared;
        this.hash = hash;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getNumOfSegments() {
        return numOfSegments;
    }

    public FlexID[] getFlexID() {
        return flexID;
    }

    public boolean isShared() {
        return shared;
    }

    public void setFlexID(FlexID[] flexID) { this.flexID = flexID; }

    public void setHash(String hash) { this.hash = hash; }

    public String getHash() { return hash; }

    public String generateHash(String path) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] content = Files.readAllBytes(Paths.get(path));
        byte[] digest = md.digest(content);

        return Base64.getEncoder().encodeToString(digest);
    }
}
