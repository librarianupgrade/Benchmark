package io.github.zeroone3010.yahueapi.v2.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.zeroone3010.yahueapi.domain.JsonStringUtil;

public class Motion {
	@JsonProperty("motion")
	private boolean motion;

	@JsonProperty("motion_valid")
	private boolean motionValid;

	@JsonProperty("motion_report")
	private MotionReport motionReport;

	public boolean isMotion() {
		return motion;
	}

	public boolean isMotionValid() {
		return motionValid;
	}

	public MotionReport getMotionReport() {
		return motionReport;
	}

	public String toString() {
		return JsonStringUtil.toJsonString(this);
	}
}
