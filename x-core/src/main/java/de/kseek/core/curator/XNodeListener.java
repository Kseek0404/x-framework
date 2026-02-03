package de.kseek.core.curator;

/**
 * @author kseek
 * @date 2024/3/22
 */
public interface XNodeListener {
    enum NodeChangeType {
        NODE_ADD, NODE_REMOVE, DATA_CHANGE
    }

    /**
     * 节点状态改变
     *
     * @param nodeChangeType
     * @param xNode
     */
    void nodeChange(NodeChangeType nodeChangeType, XNode xNode);
}
