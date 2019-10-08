## Java fast data transfer from client to multi server

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5f1f8d2a036c405d8dc713bc656561cb)](https://www.codacy.com/manual/makbn/fdt_streamfork?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=makbn/fdt_streamfork&amp;utm_campaign=Badge_Grade)
[![Known Vulnerabilities](https://snyk.io//test/github/makbn/fdt_streamfork/badge.svg?targetFile=streamfork/pom.xml)](https://snyk.io//test/github/makbn/fdt_streamfork?targetFile=streamfork/pom.xml)
[![Maintainability](https://api.codeclimate.com/v1/badges/8e059fd3fc3c2d3fb963/maintainability)](https://codeclimate.com/github/makbn/fdt_streamfork/maintainability)

Streamfork make it possible to connect input streaming data to multi source output stream directly! This project is part of POD cloud storage backend for saving user files to multi file servers. 
* Currently, the protocol that used in Streamfork is just adapted to our scenario and file nameing is fixed len (16byte).

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
        " and typesetting industry. Lorem Ipsum has been the indust" +
        "ry's standard dummy text ever since the 1500s, when an unk" +
        "nown printer took a galley of type and scrambled it to mak" +
        "e a type specimen book. It has survived not only five cent" +
        "uries, but also the leap into electronic typesetting, rema" +
        "ining essentially unchanged. It was popularised in the 196" +
        "0s with the release of Letraset sheets containing Lorem Ip" +
        "sum passages, and more recently with desktop publishing so" +
        "ftware like Aldus PageMaker including versions of Lorem Ipsum.");
printWriter.close();
InputStream fileStream = new FileInputStream(inputFile);

```
Now connecting a client to the started servers:

```java
SFClient client = SFClient.get(StreamMode.Parallel)
        .addServer("127.0.0.1", 8050)
        .addServer("127.0.0.1", 8051)
        .addServer("127.0.0.1", 8052)
        .setAutoClosable(true);

int len;
byte[] data = null;
while ((len = fileStream.available()) > 0){
    data = StreamReader.read(fileStream, len);
}

String name = UUID.randomUUID().toString().substring(0, 16);
StreamBlock block = new StreamBlock(name, data);
client.write(block);
```

In the provided example 3 servers started and a client send a file with random fixed name len (16 char) to servers and servers save file on `files` folder under the classpath directory
