package com.vikingsen.inject.fragment.processor

import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertAbout
import com.google.testing.compile.JavaFileObjects
import com.google.testing.compile.JavaSourceSubjectFactory.javaSource
import com.google.testing.compile.JavaSourcesSubjectFactory
import com.google.testing.compile.JavaSourcesSubjectFactory.javaSources
import org.junit.Test

private const val GENERATED_TYPE = "javax.annotation.Generated" // TODO vary once JDK 9 works.
private const val GENERATED_ANNOTATION = """
@Generated(
  value = "com.vikingsen.inject.fragment.processor.FragmentInjectProcessor",
  comments = "https://github.com/hansenji/FragmentInject"
)
"""

class FragmentInjectProcessorTest {

    @Test
    fun simpleTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            public class TestFragment extends Fragment {
            
                @FragmentInject
                TestFragment(Long foo) {
                    
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module(includes = FragmentInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestFragment_InjectFactory", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestFragment_InjectFactory implements FragmentInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestFragment_InjectFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Fragment create() {
                    return new TestFragment(foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.FragmentInject_TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.ClassKey;
            import dagger.multibindings.IntoMap;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class FragmentInject_TestModule {
                private FragmentInject_TestModule() {
                } 

                @Binds
                @IntoMap
                @ClassKey(TestFragment.class)
                abstract FragmentInjectFactory bind_test_TestFragment(TestFragment_InjectFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputFragment, inputModule))
            .processedWith(FragmentInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun publicTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            class TestFragment extends Fragment {
            
                @FragmentInject
                TestFragment(Long foo) {
                    
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module(includes = FragmentInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestFragment_InjectFactory", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestFragment_InjectFactory implements FragmentInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestFragment_InjectFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Fragment create() {
                    return new TestFragment(foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.FragmentInject_TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.ClassKey;
            import dagger.multibindings.IntoMap;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class FragmentInject_TestModule {
                private FragmentInject_TestModule() {
                } 

                @Binds
                @IntoMap
                @ClassKey(TestFragment.class)
                abstract FragmentInjectFactory bind_test_TestFragment(TestFragment_InjectFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputFragment, inputModule))
            .processedWith(FragmentInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun nestedTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            class Outer {
                static class TestFragment extends Fragment {
                
                    @FragmentInject
                    TestFragment(Long foo) {
                        
                    }
                }
            }
        """
        )
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module(includes = FragmentInject_TestModule.class)
            abstract class TestModule {}
        """
        )

        val expectedFactory = JavaFileObjects.forSourceString(
            "test.TestFragment_InjectFactory", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class Outer${'$'}TestFragment_InjectFactory implements FragmentInjectFactory {
                private final Provider<Long> foo;

                @Inject public Outer${'$'}TestFragment_InjectFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Fragment create() {
                    return new Outer.TestFragment(foo.get());
                }
            }
        """
        )

        val expectedModule = JavaFileObjects.forSourceString(
            "test.FragmentInject_TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import dagger.Binds;
            import dagger.Module;
            import dagger.multibindings.ClassKey;
            import dagger.multibindings.IntoMap;
            import $GENERATED_TYPE;

            @Module
            $GENERATED_ANNOTATION
            abstract class FragmentInject_TestModule {
                private FragmentInject_TestModule() {
                } 

                @Binds
                @IntoMap
                @ClassKey(Outer.TestFragment.class)
                abstract FragmentInjectFactory bind_test_Outer${'$'}TestFragment(Outer${'$'}TestFragment_InjectFactory factory);
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputFragment, inputModule))
            .processedWith(FragmentInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory, expectedModule)
    }

    @Test
    fun typeDoesNotExtendFragment() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import com.vikingsen.inject.fragment.FragmentInject;
            
            public class TestFragment {
            
                @FragmentInject
                TestFragment(Long foo) {
                    
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputFragment)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentInject-using types must be a subtype of androidx.fragment.app.Fragment
            """.trimIndent())
            .`in`(inputFragment).onLine(6)

    }

    @Test
    fun baseAndSubtypeInjectionTest() {
        val inputFragment1 = JavaFileObjects.forSourceString(
            "test.TestFragment1", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            public class TestFragment1 extends Fragment {
            
                @FragmentInject
                TestFragment1(Long foo) {
                    
                }
            }
        """
        )

        val inputFragment2 = JavaFileObjects.forSourceString(
            "test.TestFragment2", """
            package test;

            import com.vikingsen.inject.fragment.FragmentInject;
            
            public class TestFragment2 extends TestFragment1 {
            
                @FragmentInject
                TestFragment2(Long foo) {
                    super(foo);
                }
            }
        """
        )

        val expectedFactory1 = JavaFileObjects.forSourceString(
            "test.TestFragment1_InjectFactory", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestFragment1_InjectFactory implements FragmentInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestFragment1_InjectFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Fragment create() {
                    return new TestFragment1(foo.get());
                }
            }
        """
        )

        val expectedFactory2 = JavaFileObjects.forSourceString(
            "test.TestFragment2_InjectFactory", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInjectFactory;
            import java.lang.Long;
            import java.lang.Override;
            import $GENERATED_TYPE;
            import javax.inject.Inject;
            import javax.inject.Provider;

            $GENERATED_ANNOTATION
            public final class TestFragment2_InjectFactory implements FragmentInjectFactory {
                private final Provider<Long> foo;

                @Inject public TestFragment2_InjectFactory(Provider<Long> foo) {
                    this.foo = foo;
                }

                @Override public Fragment create() {
                    return new TestFragment2(foo.get());
                }
            }
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputFragment1, inputFragment2))
            .processedWith(FragmentInjectProcessor())
            .compilesWithoutError()
            .and()
            .generatesSources(expectedFactory1, expectedFactory2)
    }

    @Test
    fun privateConstructorFailsTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            public class TestFragment extends Fragment {
            
                @FragmentInject
                private TestFragment(Long foo) {
                    
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputFragment)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentInject constructor must not be private
            """.trimIndent())
            .`in`(inputFragment).onLine(7)
    }

    @Test
    fun nestedPrivateTestFailsTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            class Outer {
                private static class TestFragment extends Fragment {
                
                    @FragmentInject
                    TestFragment(Long foo) {
                        
                    }
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputFragment)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentInject-using types must not be private
            """.trimIndent())
            .`in`(inputFragment).onLine(8)
    }

    @Test
    fun nestedNonStaticTestFailsTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            class Outer {
                class TestFragment extends Fragment {
                
                    @FragmentInject
                    TestFragment(Long foo) {
                        
                    }
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputFragment)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentInject-using types must be static
            """.trimIndent())
            .`in`(inputFragment).onLine(8)
    }

    @Test
    fun multipleConstructorFailsTest() {
        val inputFragment = JavaFileObjects.forSourceString(
            "test.TestFragment", """
            package test;

            import androidx.fragment.app.Fragment;
            import com.vikingsen.inject.fragment.FragmentInject;
            
            public class TestFragment extends Fragment {
            
                @FragmentInject
                private TestFragment(Long foo) {
                    
                }
                
                @FragmentInject
                private TestFragment(String foo) {
                    
                }
            }
        """
        )

        assertAbout(javaSource())
            .that(inputFragment)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
               Multiple @FragmentInject-annotated constructors found.
            """.trimIndent())
            .`in`(inputFragment).onLine(7)
    }

    @Test
    fun moduleWithoutModuleAnnotationFailsTest() {
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;

            @FragmentModule
            abstract class TestModule {}
        """
        )

        assertAbout(javaSource())
            .that(inputModule)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentModule must also be annotated as Dagger @Module
            """.trimIndent())
            .`in`(inputModule).onLine(7)
    }

    @Test
    fun moduleWithNoIncludesFailsTest() {
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module
            abstract class TestModule {}
        """
        )

        assertAbout(javaSource())
            .that(inputModule)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentModule's @Module must include FragmentInject_TestModule
            """.trimIndent())
            .`in`(inputModule).onLine(9)
    }

    @Test
    fun moduleWithoutIncludesFailsTest() {
        val inputModule = JavaFileObjects.forSourceString(
            "test.TestModule", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module(includes = TestModule2.class)
            abstract class TestModule {}
            
            @Module
            abstract class TestModule2 {}
        """
        )

        assertAbout(javaSource())
            .that(inputModule)
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                @FragmentModule's @Module must include FragmentInject_TestModule
            """.trimIndent())
            .`in`(inputModule).onLine(9)
    }

    @Test
    fun multipleModulesFailsTest() {
        val inputModule1 = JavaFileObjects.forSourceString(
            "test.TestModule1", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module(includes = FragmentInject_TestModule1.class)
            abstract class TestModule1 {}
        """
        )

        val inputModule2 = JavaFileObjects.forSourceString(
            "test.TestModule2", """
            package test;

            import com.vikingsen.inject.fragment.FragmentModule;
            import dagger.Module;

            @FragmentModule
            @Module(includes = FragmentInject_TestModule2.class)
            abstract class TestModule2 {}
        """
        )

        assertAbout(javaSources())
            .that(listOf(inputModule1, inputModule2))
            .processedWith(FragmentInjectProcessor())
            .failsToCompile()
            .withErrorContaining("""
                Multiple @FragmentModule-annotated modules found.
            """.trimIndent())
            .`in`(inputModule1).onLine(9)
            .and()
            .withErrorContaining("""
                Multiple @FragmentModule-annotated modules found.
            """.trimIndent())
            .`in`(inputModule1).onLine(9)
    }
}