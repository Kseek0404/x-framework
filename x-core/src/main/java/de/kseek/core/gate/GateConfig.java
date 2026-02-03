package de.kseek.core.gate;

import lombok.Data;
import de.kseek.core.net.NetAddress;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Data
public class GateConfig {
    public NetAddress netAddress;
    public NetAddress wsAddress;
    public boolean wss;
    public String sslKeyPath = "";
    public String sslKeyPwd = "";
}
