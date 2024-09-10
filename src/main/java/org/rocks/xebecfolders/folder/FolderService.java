package org.rocks.xebecfolders.folder;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class FolderService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<Path, LocalDateTime> recentlyDeleted = new HashMap<>();

    public FolderService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void monitorDirectory(Path dir) throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        System.out.println("Monitoring directory: " + dir);

        while (true) {
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();

                String message = "";
                if (kind == ENTRY_CREATE) {
                    // Check if this is a rename by checking for a recent delete
                    if (recentlyDeleted.containsKey(fileName)) {
                        message = "File renamed to: " + fileName;
                        recentlyDeleted.remove(fileName); // Remove it from the map after detection
                    } else {
                        message = "File created: " + fileName;
                    }
                } else if (kind == ENTRY_DELETE) {
                    // Store the delete event and the timestamp
                    message = "File deleted: " + fileName;
                    recentlyDeleted.put(fileName, LocalDateTime.now());
                } else if (kind == ENTRY_MODIFY) {
                    message = "File modified: " + fileName;
                }

                HashMap<String, String> map = new HashMap<>();
                map.put("path", message);
                messagingTemplate.convertAndSend("/topic/messages", map);

                System.out.println(message);
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }
}




/*import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
public class FolderService {

    private final SimpMessagingTemplate messagingTemplate;

    public FolderService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void monitorDirectory(Path dir) throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        System.out.println("Monitoring directory: " + dir);

        while (true) {
            WatchKey key = watchService.take();

            // Collect all the files and folders after any event (create, delete, modify)
            LinkedList<String> directoryContents = getAllFilesAndFolders(dir);

            // Put the file list into a HashMap to be sent as the message
            Map<String, LinkedList<String>> map = new HashMap<>();
            map.put("path", directoryContents);

            // Send the file list via WebSocket to clients
            messagingTemplate.convertAndSend("/topic/messages", map);

            // Process the events
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();

                // Optional: Handle specific messages for each type of event
                String message = "";
                if (kind == ENTRY_CREATE) {
                    message = "File created: " + fileName;
                } else if (kind == ENTRY_DELETE) {
                    message = "File deleted: " + fileName;
                } else if (kind == ENTRY_MODIFY) {
                    message = "File modified: " + fileName;
                }

                System.out.println(message);
            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    // Method to get all files and folders in the directory
    private LinkedList<String> getAllFilesAndFolders(Path dir) throws IOException {
        LinkedList<String> fileList = new LinkedList<>();

        // Walk through all files and folders and add them to the LinkedList
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                fileList.add(entry.getFileName().toString());
            }
        }

        return fileList;
    }
}*/