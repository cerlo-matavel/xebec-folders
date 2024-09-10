package org.rocks.xebecfolders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rocks.xebecfolders.folder.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
class XebecFoldersApplicationTests {

    @Autowired
    private FolderService folderService;
    private static final String DEFAULT_PATH = "C:\\Users\\INAS\\Cerlo";

    @Test
    void contextLoads() throws IOException, InterruptedException {
        Path path = Path.of(DEFAULT_PATH);
        //folderService.monitorDirectory(path);
    }

}
