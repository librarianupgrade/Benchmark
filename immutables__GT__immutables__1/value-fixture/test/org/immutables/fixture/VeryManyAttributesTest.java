/*
   Copyright 2016 Immutables Authors and Contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package org.immutables.fixture;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class VeryManyAttributesTest {
	@Test
	@SuppressWarnings("CheckReturnValue")
	public void builableWhenAllSet() {
		ImmutableHugeInterface.builder().number0_0(1).number0_1(1).number0_2(1).number0_3(1).number0_4(1).number0_5(1)
				.number0_6(1).number0_7(1).number0_8(1).number0_9(1).number0_10(1).number0_11(1).number0_12(1)
				.number0_13(1).number0_14(1).number0_15(1).number0_16(1).number0_17(1).number0_18(1).number0_19(1)
				.number0_20(1).number0_21(1).number0_22(1).number0_23(1).number0_24(1).number0_25(1).number0_26(1)
				.number0_27(1).number0_28(1).number0_29(1).number0_30(1).number0_31(1).number0_32(1).number0_33(1)
				.number0_34(1).number0_35(1).number0_36(1).number0_37(1).number0_38(1).number0_39(1).number0_40(1)
				.number0_41(1).number0_42(1).number0_43(1).number0_44(1).number0_45(1).number0_46(1).number0_47(1)
				.number0_48(1).number0_49(1).number0_50(1).number0_51(1).number0_52(1).number0_53(1).number0_54(1)
				.number0_55(1).number0_56(1).number0_57(1).number0_58(1).number0_59(1).number0_60(1).number0_61(1)
				.number0_62(1).number0_63(1).number1_64(1).number1_65(1).number1_66(1).number1_67(1).number1_68(1)
				.number1_69(1).build();
	}

	@Test
	public void noInitBuildOverflow() {
		assertThrows(IllegalStateException.class, () -> createHuge());
	}

	@SuppressWarnings("CheckReturnValue")
	private static void createHuge() {
		ImmutableHugeInterface.builder().number0_0(1).number0_1(1).number0_2(1).number0_3(1).number0_4(1).number0_5(1)
				.number0_6(1).number0_7(1).number0_8(1).number0_9(1).number0_10(1).number0_11(1).number0_12(1)
				.number0_13(1).number0_14(1).number0_15(1).number0_16(1).number0_17(1).number0_18(1).number0_19(1)
				.number0_20(1).number0_21(1).number0_22(1).number0_23(1).number0_24(1).number0_25(1).number0_26(1)
				.number0_27(1).number0_28(1).number0_29(1).number0_30(1).number0_31(1)
				// .number0_32(1) // no bit overflow here
				.number0_33(1).number0_34(1).number0_35(1).number0_36(1).number0_37(1).number0_38(1).number0_39(1)
				.number0_40(1).number0_41(1).number0_42(1).number0_43(1).number0_44(1).number0_45(1).number0_46(1)
				.number0_47(1).number0_48(1).number0_49(1).number0_50(1).number0_51(1).number0_52(1).number0_53(1)
				.number0_54(1).number0_55(1).number0_56(1).number0_57(1).number0_58(1).number0_59(1).number0_60(1)
				.number0_61(1).number0_62(1).number0_63(1).number1_64(1).number1_65(1).number1_66(1).number1_67(1)
				.number1_68(1).number1_69(1).build();
	}

	@Test
	@SuppressWarnings("CheckReturnValue")
	public void noOccupationOverflow() {
		ImmutableHugeOccupationOverflow.builder().number0(0).number1(1).number2(2).number3(3).number4(4).number5(5)
				.number6(6).number7(7).number8(8).number9(9).number10(10).number11(11).number12(12).number13(13)
				.number14(14).number15(15).number16(16).number17(17).number18(18).number19(19).number20(20).number21(21)
				.number22(22).number23(23).number24(24).number25(25).number26(26).number27(27).number28(28).number29(29)
				.number30(30).number31(31).number32(32).number33(33).build();
	}
}
