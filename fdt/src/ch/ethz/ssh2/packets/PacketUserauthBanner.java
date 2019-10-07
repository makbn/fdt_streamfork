package ch.ethz.ssh2.packets;

import java.io.IOException;

/**
 * PacketUserauthBanner.
 *
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id: PacketUserauthBanner.java,v 1.2 2005/08/24 17:54:08 cplattne Exp $
 */
public class PacketUserauthBanner {
    byte[] payload;

    String message;
    String language;

    public PacketUserauthBanner(String message, String language) {
        this.message = message;
        this.language = language;
    }

    public PacketUserauthBanner(byte payload[], int off, int len) throws IOException {
        this.payload = new byte[len];
        System.arraycopy(payload, off, this.payload, 0, len);

        TypesReader tr = new TypesReader(payload, off, len);

        int packet_type = tr.readByte();

        if (packet_type != Packets.SSH_MSG_USERAUTH_BANNER)
            throw new IOException("This is not a SSH_MSG_USERAUTH_BANNER! (" + packet_type + ")");

        message = tr.readString("UTF-8");
        language = tr.readString();

        if (tr.remain() != 0)
            throw new IOException("Padding in SSH_MSG_USERAUTH_REQUEST packet!");
    }

    public String getBanner() {
        return message;
    }

    public byte[] getPayload() {
        if (payload == null) {
            TypesWriter tw = new TypesWriter();
            tw.writeByte(Packets.SSH_MSG_USERAUTH_BANNER);
            tw.writeString(message);
            tw.writeString(language);
            payload = tw.getBytes();
        }
        return payload;
    }
}
