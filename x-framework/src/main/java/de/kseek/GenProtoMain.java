package de.kseek;

import de.kseek.core.protostuff.ProtobufComment;
import de.kseek.core.protostuff.ProtobufMessage;
import de.kseek.java2pb.gen.ToOneFile;

/**
 * 生成 x-core 的 .proto 文件的入口。
 * 依赖 x-core 与 x-java2pb，由本模块提供 classpath，x-java2pb 不依赖 x-core。
 * <p>
 * 参数：searchPackage gen2dir filename [importFiles]
 * 示例：de.kseek.core gen_proto x-core.proto
 */
public class GenProtoMain {

    public static void main(String[] args) {
        if (args == null || args.length < 3) {
            System.err.println("Usage: searchPackage gen2dir filename [importFiles]");
            System.err.println("Example: de.kseek.core gen_proto x-core.proto");
            return;
        }
        String searchPackage = args[0];
        String gen2dir = args[1];
        String filename = args[2];
        String importFiles = args.length > 3 ? args[3] : null;

        ToOneFile.java2PbMessage(searchPackage, importFiles, null, gen2dir, filename,
                ProtobufMessage.class, ProtobufComment.class);
    }
}
