package zeymo.protocol.dump;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author Zeymo
 */
public abstract class Content {
    public static final Content NO_CONTENT = new Content() {
        @Override
        public byte[] bytes() {
            return new byte[0];
        }

        @Override
        public int len() {
            return 0;
        }

        @Override
        public ByteBuf rawByteBuf() {
            return null;
        }

        @Override
        public String toString() {
            return "<No content>";
        }
    };

    public static Content content(final byte[] bytes) {
        return new Content() {
            @Override
            public byte[] bytes() {
                return bytes;
            }

            @Override
            public int len() {
                return bytes.length;
            }

            @Override
            public ByteBuf rawByteBuf() {
                return null;
            }

            @Override
            public String toString() {
                return Arrays.toString(bytes) + " [" + bytes.length + ']';
            }

        };
    }

    public static Content content(final ByteBuf byteBuf) {
        return new Content() {
            byte[] contentBytes = null;

            @Override
            public byte[] bytes() {
                if (contentBytes != null) {
                    return contentBytes;
                }
                if (byteBuf == null) {
                    return null;
                }
                int readableBytes = byteBuf.readableBytes();
                if (readableBytes <= 0) {
                    return null;
                }
                byte[] rawBytes = new byte[readableBytes];
                byteBuf.readBytes(rawBytes);
                this.contentBytes = rawBytes;
                return rawBytes;
            }

            @Override
            public int len() {
                if (contentBytes != null) {
                    return contentBytes.length;
                }
                return byteBuf.readableBytes();
            }

            @Override
            public ByteBuf rawByteBuf() {
                return byteBuf;
            }

            @Override
            public String toString() {
                return byteBuf + " [" + len() + ']';
            }

        };
    }

    public static Content content(final String value) {
        return new Content() {
            @Override
            public byte[] bytes() {
                try {
                    // compatibly with android api level 7
                    return value.getBytes("UTF8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public int len() {
                return this.bytes().length;
            }

            @Override
            public ByteBuf rawByteBuf() {
                return null;
            }

            @Override
            public String toString() {
                return value;
            }
        };
    }

    public static <T> Content content(final T value, final Encoder<T> encoder) {
        return new Content() {
            @Override
            public byte[] bytes() {
                return encoder.encode(value);
            }

            @Override
            public int len() {
                return this.bytes().length;
            }

            @Override
            public ByteBuf rawByteBuf() {
                return null;
            }

            @Override
            public String toString() {
                return value.toString();
            }

        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Content) {
            Content content = (Content) obj;
            return Arrays.equals(bytes(), content.bytes());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes());
    }

    public abstract byte[] bytes();

    public abstract int len();

    public abstract ByteBuf rawByteBuf();

    public interface Encoder<T> {
        byte[] encode(T value);
    }

    public static class DelayUnzipContent extends Content {
        private static final int MAX_DECOMPRESSION_SIZE = 10 * 1024 * 1024;

        /** 4K */
        private static final int BUF_SIZE = 0x1000;

        private final byte[] origin;

        public    DelayUnzipContent(byte[] bytes) {origin = bytes;}

        @Override
        public int len() {
            return origin.length;
        }

        @Override
        public ByteBuf rawByteBuf() {
            return null;
        }

        @Override
        public byte[] bytes() {
            try {
                GZIPInputStream from = new GZIPInputStream(new ByteArrayInputStream(origin));
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                byte[] buf = new byte[BUF_SIZE];
                long total = 0;
                while (true) {
                    int r = from.read(buf);
                    if (r == -1) {
                        break;
                    }
                    out.write(buf, 0, r);
                    total += r;
                    if (total > MAX_DECOMPRESSION_SIZE) {
                        throw new IllegalStateException("zip file size too large");
                    }
                }

                from.close();
                return out.toByteArray();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
