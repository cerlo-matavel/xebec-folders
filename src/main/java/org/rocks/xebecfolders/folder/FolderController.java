package org.rocks.xebecfolders.folder;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FolderController {

    private final FolderService folderService;
    private static final String DEFAULT_PATH = "C:/Users/INAS/Cerlo";


    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public Map<String, String>  checkFolder(FolderPathRequest folderPathRequest) throws InterruptedException, IOException {
        Path path = Paths.get(folderPathRequest.path().isEmpty() ? DEFAULT_PATH : folderPathRequest.path());

        // Start monitoring the directory
        new Thread(() -> {
            try {
                folderService.monitorDirectory(path);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        HashMap<String, String> map = new HashMap<>();
        map.put("path", "Started monitoring directory: " + folderPathRequest.path());
        return map;
    }

}
