/*******************************************************************************
 * Copyright (c) 2022 Primetals Technologies Austria GmbH
 *               2022 Martin Erich Jobst
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Ulzii Jargalsaikhan
 *       - initial API and implementation and/or initial documentation
 *   Martin Jobst
 *       - validation for reserved identifiers
 *******************************************************************************/
package org.eclipse.fordiac.ide.structuredtextfunctioneditor.tests

import com.google.inject.Inject
import java.util.stream.Stream
import org.eclipse.fordiac.ide.model.datatype.helper.IecTypes.ElementaryTypes
import org.eclipse.fordiac.ide.model.typelibrary.DataTypeLibrary
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STBinaryOperator
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STCorePackage
import org.eclipse.fordiac.ide.structuredtextcore.stcore.STUnaryOperator
import org.eclipse.fordiac.ide.structuredtextcore.stcore.util.STCoreUtil
import org.eclipse.fordiac.ide.structuredtextcore.validation.STCoreValidator
import org.eclipse.fordiac.ide.structuredtextfunctioneditor.stfunction.STFunctionSource
import org.eclipse.fordiac.ide.structuredtextfunctioneditor.validation.STFunctionValidator
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.extensions.InjectionExtension
import org.eclipse.xtext.testing.util.ParseHelper
import org.eclipse.xtext.testing.validation.ValidationTestHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.^extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

import static org.junit.jupiter.params.provider.Arguments.*

@ExtendWith(InjectionExtension)
@InjectWith(STFunctionInjectorProvider)
class STFunctionValidatorTest {

	@Inject extension ParseHelper<STFunctionSource> parseHelper
	@Inject extension ValidationTestHelper

	@Test
	def void testWrongCasedIdentifierWarning() {
		'''
		FUNCTION hubert
		VAR
		    bol1 : BOOL := FALSE;
		    bol2 : BOOL := TRUE;
		END_VAR
		IF bol1 THEN
			bol1 := TRUE;
		ELSIF bol2 THEN
			bol1 := TRUE;
		ELSE
			bol1 := FALSE;
			bOl1 := FALSE;
		END_IF;
			bOl1 := 1;
		END_FUNCTION'''.parse.assertWarning(STCorePackage.Literals.ST_FEATURE_EXPRESSION,
			STFunctionValidator.WRONG_NAME_CASE)
	}

	@Test
	def void testConsecutiveUnderscoreErrorValidator() {
		'''
		FUNCTION hubert
		VAR
		    bo__l1 : BOOL := FALSE;
		    bol2 : BOOL := TRUE;
		END_VAR
		END_FUNCTION'''.parse.assertError(STCorePackage.eINSTANCE.STVarDeclaration,
			STCoreValidator.CONSECUTIVE_UNDERSCORE_IN_IDENTIFIER_ERROR)
	}

	@Test
	def void testTrailingUnderscoreErrorValidator() {
		'''
		FUNCTION hubert
		VAR
		    bol1_ : BOOL := FALSE;
		    bol2 : BOOL := TRUE;
		END_VAR
		END_FUNCTION'''.parse.assertError(STCorePackage.eINSTANCE.STVarDeclaration,
			STCoreValidator.TRAILING_UNDERSCORE_IN_IDENTIFIER_ERROR)
	}

	@Test
	def void testInvalidLeftSide() {
		'''
			FUNCTION hubert
			VAR
				int1 : INT := 3;
				int2 : INT := 10;
			END_VAR
			3 := 4;
			2+3 := 5;
			END_FUNCTION
		'''.parse.assertError(STCorePackage.eINSTANCE.STAssignmentStatement, STCoreValidator.ASSIGNMENT_INVALID_LEFT)
	}

	@Test
	def void testInvalidAssignment() {
		'''
			FUNCTION hubert
			VAR
				int1 : INT := 3;
				int2 : INT := 5;
				real1 : REAL := 3.14;
				sint1 : SINT := 4;
			END_VAR
				sint1 := int2;
				int2 := sint1;
				int1 := real;
			END_FUNCTION
		'''.parse.assertError(STCorePackage.eINSTANCE.STAssignmentStatement, STCoreValidator.NON_COMPATIBLE_TYPES)

	}

	def void testInvalidArrayAssignment() {
		'''
			FUNCTION hubert
			VAR
				testArray: ARRAY [ 0 .. 10 ] OF INT;
				testArray2: ARRAY [ 0 .. 10, 0 .. 10] OF INT;
				testArray3: ARRAY [ 0 .. 10] OF BOOL;
				testArray4: ARRAY [ 0 .. 10 ] OF REAL;
			END_VAR
			testArray := testArray2[0];
			testArray3 := testArray[1];
			testArray := testArray4;
			END_FUNCTION
		'''.parse.assertError(STCorePackage.eINSTANCE.STAssignmentStatement, STCoreValidator.NON_COMPATIBLE_TYPES)
	}

	@Test
	def void testValidAssignment() {
		'''
			FUNCTION hubert
			VAR
				int1 : INT := 3;
				int2 : INT := 10;
				bool1 : BOOL := 0;
				sint1 : SINT := 4;
			END_VAR
				int1 := int2;
				int2 := sint1;
			END_FUNCTION
		'''.parse.assertNoErrors
	}

	@Test
	def void testValidArrayAssignment() {
		'''
			FUNCTION hubert
			VAR
				testArray: ARRAY [ 0 .. 10 ] OF INT;
				testArray2: ARRAY [ 0 .. 10, 0 .. 10] OF INT;
				testArray3: ARRAY [ 0 .. 10] OF INT;
				testArray4: ARRAY [ 0 .. 10 ] OF REAL;
			END_VAR
			testArray := testArray3;
			testArray := testArray2[0];
			testArray2[0] := testArray2[1];
			testArray2[0, 2] := 5;
			END_FUNCTION
		'''.parse.assertNoErrors
	}

	@Test
	def void testNoCast() {
		'''
			FUNCTION hubert
			VAR
				lreal1 : LREAL;
				usint1 : USINT;
				word1 : WORD;
				bool1 : BOOL;
				ldt1 : LDT;
				string1 : STRING;
			END_VAR
				bool1 := usint1;
				word1 := ltime1;
			END_FUNCTION
		'''.parse.assertError(STCorePackage.eINSTANCE.STAssignmentStatement, STCoreValidator.NO_CAST_AVAILABLE)
	}

	@Test
	def void testArrayNoCast() {
		'''
			FUNCTION hubert
			VAR
				testArray: ARRAY [ 0 .. 10 ] OF BOOL;
				testArray2: ARRAY [ 0 .. 10, 0 .. 10] OF INT;
				testArray3: ARRAY [ 0 .. 10] OF INT;
				testArray4: ARRAY [ 0 .. 10 ] OF REAL;
				int1 : INT := 3;
			END_VAR
			testArray := testArray3;
			testArray := testArray2[0];
			testArray2[0] := testArray2[1];
			testArray2[0, 2] := 5;
			END_FUNCTION
		'''.parse.assertError(STCorePackage.eINSTANCE.STAssignmentStatement, STCoreValidator.NO_CAST_AVAILABLE)
	}

	@Test
	def void testCast() {
		'''
			FUNCTION hubert
			VAR
				int1 : INT;
				sint1 : SINT;
				word1 : WORD;
				byte1 : BYTE;
				char1 : CHAR;
				wchar1 : WCHAR;
				dword1 : DWORD;
				word1 : WORD; 
				byte1 : BYTE;
				ldt1 : LDT;
				ltod1 : LTOD;
			END_VAR
				ltod1 := LDT_TO_LTOD(ldt1);
				dword1 := WCHAR_TO_DWORD(wchar1);
				char1 := byte1;
				int1 := sint1;
				char1 := wchar1;
			END_FUNCTION
		'''.parse.assertNoErrors(STCorePackage.eINSTANCE.STAssignmentStatement, STCoreValidator.NO_CAST_AVAILABLE);
	}

	@Test
	def void testReservedIdentifierErrorValidator() {
		'''
		FUNCTION hubert
		VAR
		    add : BOOL;
		END_VAR
		END_FUNCTION'''.parse.assertError(STCorePackage.eINSTANCE.STVarDeclaration,
			STCoreValidator.RESERVED_IDENTIFIER_ERROR)
	}

	@ParameterizedTest(name="{index}: {0} with {1}")
	@MethodSource("typeUnaryOperatorArgumentsCartesianProvider")
	def void testUnaryOperatorNotApplicableErrorValidator(String operatorName, String typeName) {
		val operator = STUnaryOperator.getByName(operatorName)
		val type = ElementaryTypes.getTypeByName(typeName)
		val result = '''
		FUNCTION hubert
		VAR
		    var1 : «type.name»;
		END_VAR
		var1 := «operator.literal» var1;
		END_FUNCTION'''.parse
		if (STCoreUtil.isApplicableTo(operator, type))
			result.assertNoErrors
		else
			result.assertError(STCorePackage.eINSTANCE.STUnaryExpression, STCoreValidator.OPERATOR_NOT_APPLICABLE)
	}

	@ParameterizedTest(name="{index}: {0} with {1} and {2}")
	@MethodSource("typeBinaryOperatorArgumentsCartesianProvider")
	def void testBinaryOperatorNotApplicableErrorValidator(String operatorName, String leftTypeName,
		String rightTypeName) {
		val operator = STBinaryOperator.getByName(operatorName)
		val leftType = ElementaryTypes.getTypeByName(leftTypeName)
		val rightType = ElementaryTypes.getTypeByName(rightTypeName)
		val result = '''
		FUNCTION hubert
		VAR
		    var1 : «leftType.name»;
		    var2 : «rightType.name»;
		    var3 : BOOL;
		END_VAR
		var3 := (var1 «operator.literal» var2) = var1;
		END_FUNCTION'''.parse
		if (STCoreUtil.isApplicableTo(operator, leftType, rightType))
			result.assertNoErrors
		else
			result.assertError(STCorePackage.eINSTANCE.STBinaryExpression, STCoreValidator.OPERATOR_NOT_APPLICABLE)
	}

	def static Stream<Arguments> typeUnaryOperatorArgumentsCartesianProvider() {
		DataTypeLibrary.nonUserDefinedDataTypes.stream.flatMap [ type |
			STUnaryOperator.VALUES.stream.map [ op |
				arguments(op.getName, type.name)
			]
		]
	}

	def static Stream<Arguments> typeBinaryOperatorArgumentsCartesianProvider() {
		DataTypeLibrary.nonUserDefinedDataTypes.stream.flatMap [ first |
			DataTypeLibrary.nonUserDefinedDataTypes.stream.flatMap [ second |
				STBinaryOperator.VALUES.stream.map [ op |
					arguments(op.getName, first.name, second.name)
				]
			]
		]
	}
}
