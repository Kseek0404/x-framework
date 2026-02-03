package de.kseek.core.gate;

import lombok.Data;

/**
 * @author kseek
 * @date 2024/3/22
 */
@Data
public class GateInfo {
    /**
     * 网关配置
     */
    private GateConfig gateConfig;
    /**
     * 人数
     */
    private int nop;

    public GateInfo() {
    }

    public GateInfo(GateConfig gateConfig, int nop) {
        this.gateConfig = gateConfig;
        this.nop = nop;
    }
}
