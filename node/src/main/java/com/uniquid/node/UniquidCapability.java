package com.uniquid.node;

import org.spongycastle.util.encoders.Hex;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class UniquidCapability {

    private String assigner; // Authority (owner) Address

    private String resourceID; // serviceProviderAddress
    private String assignee; // serviceUserAddress
    private byte[] rights;
    private long since;
    private long until;

    private String assignerSignature;

    private UniquidCapability() {}

    public String getAssignerSignature() {
        return assignerSignature;
    }

    public void setAssignerSignature(String assignerSignature) {
        this.assignerSignature = assignerSignature;
    }

    public String getResourceID() {
        return resourceID;
    }

    public String getAssigner() {
        return assigner;
    }

    public String getAssignee() {
        return assignee;
    }

    public byte[] getRights() {
        return rights;
    }

    public long getSince() {
        return since;
    }

    public long getUntil() {
        return until;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof UniquidCapability))
            return false;

        if (this == object)
            return true;

        final UniquidCapability other = (UniquidCapability) object;

        return Objects.equals(resourceID, other.resourceID) &&
                Objects.equals(assigner, other.assigner) &&
                Objects.equals(assignee, other.assignee) &&
                Arrays.equals(rights, other.rights) &&
                Objects.equals(since, other.since) &&
                Objects.equals(until, other.until);

    }

    @Override
    public String toString() {

        SimpleDateFormat s = new SimpleDateFormat();

        return String.format("resourceID %s, assigner %s, assignee %s, rights %s, start %s end %s", resourceID, assigner, assignee, Hex.toHexString(rights), s.format(new Date(since)), s.format(new Date(until)));

    }

    public String prepareToSign() {

        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(assigner);

        stringBuffer.append(resourceID);
        stringBuffer.append(assignee);
        stringBuffer.append(Hex.toHexString(rights));
        stringBuffer.append(since);
        stringBuffer.append(until);

        return stringBuffer.toString();

    }

    /**
     * Builder for UniquidCapability
     *
     */
    public static class UniquidCapabilityBuilder {

        protected UniquidCapability uniquidCapability;

        public UniquidCapabilityBuilder() {

            this.uniquidCapability = new UniquidCapability();

        }

        public UniquidCapabilityBuilder setResourceID(String resourceID) {
            uniquidCapability.resourceID = resourceID;

            return this;
        }

        public UniquidCapabilityBuilder setAssigner(String assigner) {
            uniquidCapability.assigner = assigner;
            return this;
        }

        public UniquidCapabilityBuilder setAssignee(String assignee) {
            uniquidCapability.assignee = assignee;
            return this;
        }

        public UniquidCapabilityBuilder setRights(byte[] rights) {
            uniquidCapability.rights = rights;
            return this;
        }

        public UniquidCapabilityBuilder setSince(long since) {
            uniquidCapability.since = since;
            return this;
        }

        public UniquidCapabilityBuilder setUntil(long until) {
            uniquidCapability.until = until;
            return this;
        }

        public UniquidCapabilityBuilder setAssignerSignature(String assignerSignature) {
            uniquidCapability.assignerSignature = assignerSignature;
            return this;
        }

        /**
         * Creates a new capablity. The rights must but 19 bytes
         * @return a new {@link UniquidCapability}
         * @throws Exception in case a problem occurs
         */
        public UniquidCapability build() throws Exception {

            if (uniquidCapability.resourceID == null ||
                    uniquidCapability.assigner == null ||
                    uniquidCapability.assignee == null ||
                    uniquidCapability.rights == null ||
                    uniquidCapability.rights.length != 19 ||
                    uniquidCapability.since == 0 ||
                    uniquidCapability.until == 0) {
                throw new Exception("Capability contains ivalid fields");
            }

            return uniquidCapability;

        }

    }

}
