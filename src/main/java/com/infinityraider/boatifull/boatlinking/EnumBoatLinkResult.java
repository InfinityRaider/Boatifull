package com.infinityraider.boatifull.boatlinking;

public enum EnumBoatLinkResult {
    SUCCESS_START(true),
    SUCCESS_FINISH(true),
    FAIL_TOO_FAR(false),
    FAIL_ALREADY_HAS_LEADER(false),
    FAIL_LINK_LOOP(false),
    FAIL_PLAYER_ALREADY_LINKING(false),
    FAIL_BOAT_ALREADY_LINKING(false),
    FAIL_NOT_LINKING(false);

    private boolean ok;

    EnumBoatLinkResult(boolean ok) {
        this.ok = ok;
    }

    public boolean isOk() {
        return ok;
    }
}
