package nl.jqno.equalsverifier.coverage;

import nl.jqno.equalsverifier.testhelpers.types.Color;

/**
 * equals and hashCode generated by NetBeans IDE 7.3.
 */
// CHECKSTYLE: ignore NoWhitespaceAfter for 35 lines.
public final class NetBeansGetClassPoint {
	private final int x;
	private final int y;
	private final Color color;

	public NetBeansGetClassPoint(int x, int y, Color color) {
		this.x = x;
		this.y = y;
		this.color = color;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 83 * hash + this.x;
		hash = 83 * hash + this.y;
		hash = 83 * hash + (this.color != null ? this.color.hashCode() : 0);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NetBeansGetClassPoint other = (NetBeansGetClassPoint) obj;
		if (this.x != other.x) {
			return false;
		}
		if (this.y != other.y) {
			return false;
		}
		if (this.color != other.color) {
			return false;
		}
		return true;
	}
}
