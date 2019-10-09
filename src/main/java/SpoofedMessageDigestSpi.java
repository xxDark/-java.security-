import java.security.MessageDigestSpi;

public final class SpoofedMessageDigestSpi extends MessageDigestSpi {

    @Override
    protected void engineUpdate(byte input) {
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
    }

    @Override
    protected byte[] engineDigest() {
        return new byte[16];
    }

    @Override
    protected void engineReset() {
    }
}
