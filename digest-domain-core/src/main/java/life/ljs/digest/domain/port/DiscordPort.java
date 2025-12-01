package life.ljs.digest.domain.port;

import life.ljs.digest.domain.model.DigestBatch;

public interface DiscordPort {
    /**
     * 将本次Digest推送到Discord
     * @param digestBatch
     */
    void pushDigest(DigestBatch digestBatch);
}
