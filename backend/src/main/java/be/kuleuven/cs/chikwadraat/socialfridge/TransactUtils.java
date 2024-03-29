package be.kuleuven.cs.chikwadraat.socialfridge;

import com.googlecode.objectify.Objectify;

import java.util.ConcurrentModificationException;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

/**
 * Transaction utilities.
 */
public class TransactUtils {

    private TransactUtils() {
    }

    /**
     * Like {@link com.googlecode.objectify.Objectify#transact(com.googlecode.objectify.Work)},
     * but with the possibility to throw one kind of checked exceptions.
     *
     * @param ofy  The Objectify instance to execute the transaction.
     * @param work Defines the work to be done in a transaction.
     * @return The result of the transaction.
     * @throws E Propagated checked exception from the transaction.
     */
    @SuppressWarnings("unchecked")
    public static <R, E extends Exception> R transact(Objectify ofy, final Work<R, E> work) throws E {
        try {
            return ofy.transact(new com.googlecode.objectify.Work<R>() {
                @Override
                public R run() {
                    try {
                        return work.run();
                    } catch (ConcurrentModificationException e) {
                        // Concurrent modifications are handled by Objectify
                        throw e;
                    } catch (Throwable e) {
                        // Wrap
                        throw new WrappedException(e);
                    }
                }
            });
        } catch (WrappedException e) {
            // Unwrap
            throw (E) e.getCause();
        }
    }

    /**
     * Like {@link com.googlecode.objectify.Objectify#transact(com.googlecode.objectify.Work)},
     * but with the possibility to throw one kind of checked exceptions.
     * <p>Executes the transaction on the default {@link com.googlecode.objectify.ObjectifyService#ofy() ofy()} instance.</p>
     *
     * @param work Defines the work to be done in a transaction.
     * @return The result of the transaction.
     * @throws E Propagated checked exception from the transaction.
     * @see {@link #transact(com.googlecode.objectify.Objectify, TransactUtils.Work)}
     */
    public static <R, E extends Exception> R transact(final Work<R, E> work) throws E {
        return transact(ofy(), work);
    }

    /**
     * A unit of work for a transaction.
     *
     * @param <R> The result of the transaction.
     * @param <E> The (checked) exception thrown by the transaction.
     */
    public static interface Work<R, E extends Exception> {
        public R run() throws E;
    }

    /**
     * A unit of work for a transaction with no results.
     *
     * @param <E> The (checked) exception thrown by the transaction.
     */
    public static abstract class VoidWork<E extends Exception> implements Work<Void, E> {

        @Override
        public final Void run() throws E {
            vrun();
            return null;
        }

        public abstract void vrun() throws E;

    }

    /**
     * Helper class to wrap an exception as a runtime exception.
     */
    private static class WrappedException extends RuntimeException {
        private WrappedException(Throwable cause) {
            super(cause);
        }
    }

}
