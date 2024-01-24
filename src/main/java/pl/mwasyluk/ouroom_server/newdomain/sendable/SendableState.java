package pl.mwasyluk.ouroom_server.newdomain.sendable;

public enum SendableState {
    CREATED, SENT, DELIVERED, READ;

    private static final SendableState[] VALUES = values();

    public SendableState next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }
}
