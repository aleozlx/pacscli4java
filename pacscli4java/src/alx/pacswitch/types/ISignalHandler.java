package alx.pacswitch.types;

public interface ISignalHandler {
	/**
	 * Handle an untracked message
	 * @param from Sender ID
	 * @param message Message content
	 */
	public void handleSignal(String from, String message);
}
