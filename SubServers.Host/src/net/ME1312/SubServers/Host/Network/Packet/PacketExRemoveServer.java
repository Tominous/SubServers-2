package net.ME1312.SubServers.Host.Network.Packet;

import net.ME1312.SubServers.Host.Library.Config.YAMLSection;
import net.ME1312.SubServers.Host.Library.Log.Logger;
import net.ME1312.SubServers.Host.Library.Util;
import net.ME1312.SubServers.Host.Library.Version.Version;
import net.ME1312.SubServers.Host.Network.PacketIn;
import net.ME1312.SubServers.Host.Network.PacketOut;
import net.ME1312.SubServers.Host.Network.SubDataClient;
import net.ME1312.SubServers.Host.ExHost;

import java.lang.reflect.Field;

/**
 * Create Server Packet
 */
public class PacketExRemoveServer implements PacketIn, PacketOut {
    private ExHost host;
    private int response;
    private String message;
    private String id;
    private Logger log = null;

    /**
     * New PacketExRemoveServer (In)
     *
     * @param host SubPlugin
     */
    public PacketExRemoveServer(ExHost host) {
        if (Util.isNull(host)) throw new NullPointerException();
        this.host = host;
        try {
            Field f = SubDataClient.class.getDeclaredField("log");
            f.setAccessible(true);
            this.log = (Logger) f.get(null);
            f.setAccessible(false);
        } catch (IllegalAccessException | NoSuchFieldException e) {}
    }

    /**
     * New PacketExRemoveServer (Out)
     *
     * @param response Response ID
     * @param message Message
     * @param id Receiver ID
     */
    public PacketExRemoveServer(int response, String message, String id) {
        if (Util.isNull(response, message)) throw new NullPointerException();
        this.response = response;
        this.message = message;
        this.id = id;
    }

    @Override
    public YAMLSection generate() {
        YAMLSection data = new YAMLSection();
        if (id != null) data.set("id", id);
        data.set("r", response);
        data.set("m", message);
        return data;
    }

    @Override
    public void execute(YAMLSection data) {
        try {
            if (!host.servers.keySet().contains(data.getRawString("server").toLowerCase())) {
                host.subdata.sendPacket(new PacketExRemoveServer(0, "Server Didn't Exist", (data.contains("id"))?data.getRawString("id"):null));
            } else if (host.servers.get(data.getRawString("server").toLowerCase()).isRunning()) {
                host.subdata.sendPacket(new PacketExRemoveServer(2, "That server is still running.", (data.contains("id"))?data.getRawString("id"):null));
            } else {
                host.servers.remove(data.getRawString("server").toLowerCase());
                log.info.println("Removed SubServer: " + data.getRawString("server"));
                host.subdata.sendPacket(new PacketExRemoveServer(0, "Server Removed Successfully", (data.contains("id"))?data.getRawString("id"):null));
            }
        } catch (Throwable e) {
            host.subdata.sendPacket(new PacketExRemoveServer(1, e.getClass().getCanonicalName() + ": " + e.getMessage(), (data.contains("id"))?data.getRawString("id"):null));
            host.log.error.println(e);
        }
    }

    @Override
    public Version getVersion() {
        return new Version("2.11.0a");
    }
}