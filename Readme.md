## Java fast data transfer from client to multi server

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5f1f8d2a036c405d8dc713bc656561cb)](https://www.codacy.com/manual/makbn/fdt_streamfork?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=makbn/fdt_streamfork&amp;utm_campaign=Badge_Grade)
[![Known Vulnerabilities](https://snyk.io//test/github/makbn/fdt_streamfork/badge.svg?targetFile=streamfork/pom.xml)](https://snyk.io//test/github/makbn/fdt_streamfork?targetFile=streamfork/pom.xml)
[![Maintainability](https://api.codeclimate.com/v1/badges/8e059fd3fc3c2d3fb963/maintainability)](https://codeclimate.com/github/makbn/fdt_streamfork/maintainability)

Streamfork makes it possible to connect input streaming data to multi-source Outputstream directly! This project is a part of POD cloud storage's backup method for saving users' files to multi file-servers.

### Server example

Starting three server on `localhost` from port 8050 to 8052:
```java
int TEST_SERVER_COUNT = 3;
int PORT_RANGE = 8050;
ExecutorService executorService = Executors.newFixedThreadPool(TEST_SERVER_COUNT);
for (int i = 0; i < TEST_SERVER_COUNT; i++){
    int finalI = i;
    executorService.execute(() -> new SFServer().start("127.0.0.1", (PORT_RANGE + finalI), 100));
}
```

### Client example

Let's create a temp file for sending to the servers:

```java
File inputFile = File.createTempFile("temp", "txt");
FileWriter fileWriter = new FileWriter(inputFile);
PrintWriter printWriter = new PrintWriter(fileWriter);
printWriter.print("Lorem Ipsum is simply dummy text of the printing" +
        " and typesetting industry.");
printWriter.close();
InputStream fileStream = new FileInputStream(inputFile);

//for read directly from stream
InputStream fileStream2 = new FileInputStream(inputFile);

int len;
byte[] data = null;
while ((len = fileStream.available()) > 0){
    data = StreamReader.read(fileStream, len);
}

```
Let's connecting a client to the started servers:

```java
SFClient client = SFClient.get(StreamMode.Parallel)
        .addServer("127.0.0.1", 8050)
        .addServer("127.0.0.1", 8051)
        .addServer("127.0.0.1", 8052)
        .setAutoClosable(true);
        
//read file content from byte array
String name = UUID.randomUUID().toString().substring(0, 16);
StreamBlock block = new StreamBlock(name, data);
client.write(block);

//or read from InputStream
String name2 = UUID.randomUUID().toString().substring(0, 16);
StreamBlock block2 = new StreamBlock(name2, fileStream2);
client.write(block2);
```

In the provided example, 3 servers started and a client sends a file with random fixed name length (16 char) to servers and each server saves file on `files` folder under the classpath directory.
