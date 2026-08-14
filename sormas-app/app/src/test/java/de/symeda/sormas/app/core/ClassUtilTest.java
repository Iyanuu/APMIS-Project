package de.symeda.sormas.app.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Specification for {@link ClassUtil}, which the adapter registration path uses to decide whether a binder
 * class can be instantiated reflectively.
 *
 * <p>
 * Everything here passes. The class is included because it is on that registration path: when it answers
 * wrongly the failure surfaces later as a reflective instantiation error, a long way from the cause.
 */
public class ClassUtilTest {

	public static class WithPublicNoArgConstructor {
	}

	public static class WithOnlyAnArgumentConstructor {

		public WithOnlyAnArgumentConstructor(String required) {
		}
	}

	public static class WithBothConstructors {

		public WithBothConstructors() {
		}

		public WithBothConstructors(String optional) {
		}
	}

	public static class WithOnlyAPrivateConstructor {

		private WithOnlyAPrivateConstructor() {
		}
	}

	public interface AnInterface {
	}

	@Test
	public void findsAPublicNoArgConstructor() {

		assertThat(ClassUtil.hasParameterlessPublicConstructor(WithPublicNoArgConstructor.class), is(true));
	}

	@Test
	public void findsANoArgConstructorAlongsideOthers() {

		assertThat(ClassUtil.hasParameterlessPublicConstructor(WithBothConstructors.class), is(true));
	}

	@Test
	public void rejectsAClassNeedingConstructorArguments() {

		assertThat(ClassUtil.hasParameterlessPublicConstructor(WithOnlyAnArgumentConstructor.class), is(false));
	}

	@Test
	public void rejectsANonPublicConstructor() {

		// getConstructors() only reports public ones, so a private no-arg constructor is correctly not
		// treated as usable for reflective instantiation.
		assertThat(ClassUtil.hasParameterlessPublicConstructor(WithOnlyAPrivateConstructor.class), is(false));
	}

	@Test
	public void rejectsAnInterface() {

		assertThat(ClassUtil.hasParameterlessPublicConstructor(AnInterface.class), is(false));
	}

	@Test(expected = NullPointerException.class)
	public void rejectsANullClass() {

		ClassUtil.hasParameterlessPublicConstructor(null);
	}
}
