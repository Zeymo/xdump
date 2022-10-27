package zeymo.protocol.dump;

import com.google.common.io.BaseEncoding;
import io.netty.util.ReferenceCountUtil;
import zeymo.ui.Decoration;
import zeymo.ui.TableElement;
import zeymo.ui.util.RenderUtil;

import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

import static zeymo.ui.Element.label;

/**
 * @author Zeymo
 */
public class AnsiPrinter {

    public static String channelString(SocketAddress srcAddress, SocketAddress dstAddress) {
        return String.format("[S:%s -> R:%s]", srcAddress, dstAddress);
    }

    public static void print(String requestId, String transport, Message message, String peer) {
        if (message != null) {
            TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
            table.row(label("channel").style(Decoration.bold.bold()), label(peer));
            table.row(label("requestId").style(Decoration.bold.bold()), label(requestId));
            table.row(label("transport").style(Decoration.bold.bold()), label(transport));
            if (message instanceof Request) {
                table.row(label("starLine").style(Decoration.bold.bold()), label(message.startLine().toString()));
            } else {
                table.row(label("status").style(Decoration.bold.bold()), label(message.startLine().toString()));
            }
            Map<String, List<String>> headers = message.headers();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    table.row(label(entry.getKey()).style(Decoration.bold.bold()), label(value));
                }
            }
            table.row(label("len").style(Decoration.bold.bold()), label(String.valueOf(message.body().len())));
            table.row(label("data").style(Decoration.bold.bold()), label(BaseEncoding.base64().encode(message.body().bytes())));
            System.out.println(RenderUtil.render(table, 160));
            ReferenceCountUtil.safeRelease(message.body().rawByteBuf());
        }
    }
}
