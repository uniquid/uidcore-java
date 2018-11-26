/*
 * Copyright (c) 2016-2018. Uniquid Inc. or its affiliates. All Rights Reserved.
 *
 * License is in the "LICENSE" file accompanying this file.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.uniquid.node.exception;

/**
 * Signal that a problem inside the Uniquid Node occurred
 */
public class NodeException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            getMessage() method).
     */
    public NodeException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with null as its detail message
     *
     * @param message
     *            the detail message (which is saved for later retrieval by the
     *            getMessage() method).
     * @param cause
     *            the cause (which is saved for later retrieval by the
     *            getCause() method). (A null value is permitted, and indicates
     *            that the cause is nonexistent or unknown.)
     */
    public NodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
