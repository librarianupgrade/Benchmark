package edu.jiangxin.apktoolbox.file.password.recovery.bruteforce;

import edu.jiangxin.apktoolbox.file.password.recovery.State;
import edu.jiangxin.apktoolbox.file.password.recovery.Synchronizer;
import edu.jiangxin.apktoolbox.file.password.recovery.checker.IChecker;
import edu.jiangxin.apktoolbox.file.password.recovery.exception.UnsupportedVersionException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BruteForceRunnable implements Runnable {
	private static final Logger logger = LogManager.getLogger(BruteForceRunnable.class.getSimpleName());
	private final int taskId;
	private final BruteForceFuture bruteForceFuture;
	private final BruteForceTaskParam consts;
	private final Synchronizer synchronizer;

	public BruteForceRunnable(int taskId, BruteForceTaskParam consts, BruteForceFuture bruteForceFuture,
			Synchronizer synchronizer) {
		this.taskId = taskId;
		this.consts = consts;
		this.bruteForceFuture = bruteForceFuture;
		this.synchronizer = synchronizer;
	}

	@Override
	public void run() {
		long rangeBegin = taskId * consts.getPasswordSubRangeSize();
		long rangeEnd = (taskId + 1) * consts.getPasswordSubRangeSize() - 1;
		String passwordOrNull = findPasswordInRange(rangeBegin, rangeEnd, consts.getChecker());
		bruteForceFuture.set(passwordOrNull);
	}

	/*
	 * The findPasswordInRange method find the original password using md5 hash function
	 * if a thread discovers the password, it returns original password string; otherwise, it returns null;
	 */
	public String findPasswordInRange(long rangeBegin, long rangeEnd, IChecker checker) {
		int[] passwordIterator = new int[consts.getPasswordLength()];
		transformDecToBaseN(rangeBegin, passwordIterator, consts.getCharsSet().length());
		for (long iterator = rangeBegin; iterator <= rangeEnd; iterator++) {
			if (bruteForceFuture.isDone()) {
				logger.info("isDone: " + Thread.currentThread().getName());
				return null;
			}
			if (bruteForceFuture.isCancelled()) {
				logger.info("isCancelled: " + Thread.currentThread().getName());
				return null;
			}
			String password = transformIntToStr(passwordIterator, consts.getCharsSet());
			int result;
			try {
				result = checker.checkPassword(password) ? 1 : 0;
			} catch (UnsupportedVersionException e) {
				logger.error("UnsupportedVersionException, stop");
				result = -1;
			} catch (Exception e) {
				logger.error("Exception, stop", e);
				result = -1;
			} finally {
				synchronizer.setCurrentPassword(password);
				synchronizer.increaseProgressBarValue();
			}
			if (result == 0) {
				getNextCandidate(passwordIterator, consts.getCharsSet().length());
			} else if (result == 1) {
				return password;
			} else {
				return null;
			}
		}
		return null;
	}

	/*
	 * The transformDecToBaseN transforms decimal into numArray that is base N number system
	 * If you don't understand, refer to the homework01 overview
	 */
	protected void transformDecToBaseN(long numInDec, int[] numArrayInBaseN, final int charSetLength) {
		for (int index = consts.getPasswordLength() - 1; index >= 0; index--) {
			numArrayInBaseN[index] = (int) (numInDec % charSetLength);
			numInDec = numInDec / charSetLength;
		}
	}

	/*
	 * The getNextCandidate update the possible password represented by N base system
	 */
	private void getNextCandidate(int[] candidateChars, final int charsSetLength) {
		int reminder = 1;
		for (int index = consts.getPasswordLength() - 1; index >= 0; index--) {
			candidateChars[index] += reminder;
			reminder = candidateChars[index] / charsSetLength;
			candidateChars[index] %= charsSetLength;
		}
	}

	/*
	 * We assume that each character can be represented to a number : 0 (0) , 1 (1), 2 (2) ... a (10), b (11), c (12), ... x (33), y (34), z (35)
	 * The transformIntToStr transforms int-array into string (numbers and lower-case alphabets)
	 * int array is password represented by base-N system
	 * return : password String
	 *
	 * For example, if you write the code like this,
	 *     int[] pwdBase36 = {10, 11, 12, 13, 0, 1, 9, 2};
	 *     String password = transfromIntoStr(pwdBase36);
	 *     System.out.println(password);
	 *     output is abcd0192.
	 *
	 */
	private static String transformIntToStr(int[] charsArray, final String charsSet) {
		char[] password = new char[charsArray.length];
		for (int i = 0; i < password.length; i++) {
			password[i] = charsSet.charAt(charsArray[i]);
		}
		return new String(password);
	}
}
