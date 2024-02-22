package io.github.azagniotov.stubby4j.cli;

import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.ParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import java.util.Map;
import static com.google.common.truth.Truth.assertThat;

public class CommandLineInterpreterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testIsHelpWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-h" });
        final boolean isHelp = commandLineInterpreter.isHelp();
        assertThat(isHelp).isTrue();
    }

    @Test
    public void testIsHelpWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--help" });
        final boolean isHelp = commandLineInterpreter.isHelp();
        assertThat(isHelp).isTrue();
    }

    @Test
    public void testIsVersionWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-v" });
        final boolean isVersion = commandLineInterpreter.isVersion();
        assertThat(isVersion).isTrue();
    }

    @Test
    public void testIsVersionWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--version" });
        final boolean isVersion = commandLineInterpreter.isVersion();
        assertThat(isVersion).isTrue();
    }

    @Test
    public void testIsDebugWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--debug" });
        final boolean isDebug = commandLineInterpreter.isDebug();
        assertThat(isDebug).isTrue();
    }

    @Test
    public void testIsDebugWhenShortgOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-o" });
        final boolean isDebug = commandLineInterpreter.isDebug();
        assertThat(isDebug).isTrue();
    }

    @Test
    public void testIsCacheDisabledWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--disable_stub_caching" });
        final boolean isCacheDisabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_STUB_CACHING);
        assertThat(isCacheDisabled).isTrue();
    }

    @Test
    public void testIsCacheDisabledWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-dc" });
        final boolean isCacheDisabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_STUB_CACHING);
        assertThat(isCacheDisabled).isTrue();
    }

    @Test
    public void testIsAdminPortalDisabledWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--disable_admin_portal" });
        final boolean isAdminDisabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_ADMIN);
        assertThat(isAdminDisabled).isTrue();
    }

    @Test
    public void testIsAdminPortalDisabledWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-da" });
        final boolean isAdminDisabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_ADMIN);
        assertThat(isAdminDisabled).isTrue();
    }

    @Test
    public void testIsSslDisabledWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--disable_ssl" });
        final boolean isSslDisabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL);
        assertThat(isSslDisabled).isTrue();
    }

    @Test
    public void testIsSslDisabledWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-ds" });
        final boolean isSslDisabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL);
        assertThat(isSslDisabled).isTrue();
    }

    @Test
    public void testIsMuteWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-m" });
        final boolean isMuteProvided = commandLineInterpreter.isMute();
        assertThat(isMuteProvided).isTrue();
    }

    @Test
    public void testIsMuteWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--mute" });
        final boolean isMuteProvided = commandLineInterpreter.isMute();
        assertThat(isMuteProvided).isTrue();
    }

    @Test
    public void testIsYamlProvidedWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-d", "somefilename.yaml" });
        final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();
        assertThat(isYamlProvided).isTrue();
    }

    @Test
    public void testIsYamlProvidedWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--data", "somefilename.yaml" });
        final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();
        assertThat(isYamlProvided).isTrue();
    }

    @Test
    public void testtHasAdminPortWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-a", "888" });
        final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADMINPORT);
        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testHasAdminPortWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--admin", "8888" });
        final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADMINPORT);
        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testtHasStubsPortWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-s", "888" });
        final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_CLIENTPORT);
        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testtHasStubsPortWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--stubs", "8888" });
        final boolean isAdmin = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_CLIENTPORT);
        assertThat(isAdmin).isTrue();
    }

    @Test
    public void testtIsWatchWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-w" });
        final boolean isWatch = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_WATCH);
        assertThat(isWatch).isTrue();
    }

    @Test
    public void testIsWatchWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--watch" });
        final boolean isWatch = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_WATCH);
        assertThat(isWatch).isTrue();
    }

    @Test
    public void testtHasKeystoreLocationWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-k", "some/path/to/key" });
        final boolean isKeystore = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYSTORE);
        assertThat(isKeystore).isTrue();
    }

    @Test
    public void testHasKeystoreLocationWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--keystore", "some/path/to/key" });
        final boolean isKeystore = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYSTORE);
        assertThat(isKeystore).isTrue();
    }

    @Test
    public void testtHasLocationWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-l", "hostname" });
        final boolean isLocation = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADDRESS);
        assertThat(isLocation).isTrue();
    }

    @Test
    public void testHasLocationWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--location", "hostname" });
        final boolean isLocation = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ADDRESS);
        assertThat(isLocation).isTrue();
    }

    @Test
    public void testHasPasswordWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-p", "very-complex-password" });
        final boolean isPassword = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYPASS);
        assertThat(isPassword).isTrue();
    }

    @Test
    public void testHasPasswordWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--password", "very-complex-password" });
        final boolean isPassword = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_KEYPASS);
        assertThat(isPassword).isTrue();
    }

    @Test
    public void testHasSslPortWhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-t", "2443" });
        final boolean isSslGiven = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_TLSPORT);
        assertThat(isSslGiven).isTrue();
    }

    @Test
    public void testHasSslPortWhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--tls", "2443" });
        final boolean isSslGiven = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_TLSPORT);
        assertThat(isSslGiven).isTrue();
    }

    @Test
    public void enabledTlsWithAlpnHttp2WhenShortOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-ta" });
        final boolean isTlsWithAlpnEnabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);
        assertThat(isTlsWithAlpnEnabled).isTrue();
    }

    @Test
    public void enabledTlsWithAlpnHttp2WhenLongOptionGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--enable_tls_with_alpn_and_http_2" });
        final boolean isTlsWithAlpnEnabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);
        assertThat(isTlsWithAlpnEnabled).isTrue();
    }

    @Test
    public void shouldRemoveOptionTlsWithAlpnHttp2WhenDisableSslGiven() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--enable_tls_with_alpn_and_http_2", "--disable_ssl" });
        final boolean isTlsWithAlpnEnabled = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_ENABLE_TLS_WITH_ALPN_AND_HTTP_2);
        assertThat(isTlsWithAlpnEnabled).isFalse();
        final boolean disableSsl = commandLineInterpreter.getCommandlineParams().containsKey(CommandLineInterpreter.OPTION_DISABLE_SSL);
        assertThat(disableSsl).isTrue();
    }

    @Test
    public void shouldBeFalseThatYamlIsNotProvided() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "cheburashka", "zagniotov" });
        final boolean isYamlProvided = commandLineInterpreter.isYamlProvided();
        assertThat(isYamlProvided).isFalse();
    }

    @Test
    public void shouldFailOnInvalidCommandlineLongOptionString() throws Exception {
        expectedException.expect(ParseException.class);
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--cheburashka" });
    }

    @Test
    public void shouldFailOnInvalidCommandlineShortOptionString() throws Exception {
        expectedException.expect(ParseException.class);
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-z" });
    }

    @Test
    public void shouldFailOnMissingArgumentForExistingShortOption() throws Exception {
        expectedException.expect(MissingArgumentException.class);
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "-a" });
    }

    @Test
    public void shouldFailOnMissingArgumentForExistingLongOption() throws Exception {
        expectedException.expect(MissingArgumentException.class);
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--data" });
    }

    @Test
    public void shouldReturnOneCommandlineParamWhenHelpArgPresent() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--help" });
        final Map<String, String> params = commandLineInterpreter.getCommandlineParams();
        assertThat(params.size()).isEqualTo(1);
    }

    @Test
    public void shouldReturnEmptyCommandlineParams() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] {});
        final Map<String, String> params = commandLineInterpreter.getCommandlineParams();
        assertThat(params).isEmpty();
    }

    @Test
    public void shouldReturnCommandlineParams() throws Exception {
        final CommandLineInterpreter commandLineInterpreter = new CommandLineInterpreter();
        commandLineInterpreter.parseCommandLine(new String[] { "--data", "somefilename.yaml", "-s", "12345", "--admin", "567" });
        final Map<String, String> params = commandLineInterpreter.getCommandlineParams();
        assertThat(params.size()).isEqualTo(3);
    }

    @org.openjdk.jmh.annotations.State(org.openjdk.jmh.annotations.Scope.Thread)
    public static class _Benchmark extends se.chalmers.ju2jmh.api.JU2JmhBenchmark {

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsHelpWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsHelpWhenShortOptionGiven, this.description("testIsHelpWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsHelpWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsHelpWhenLongOptionGiven, this.description("testIsHelpWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsVersionWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsVersionWhenShortOptionGiven, this.description("testIsVersionWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsVersionWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsVersionWhenLongOptionGiven, this.description("testIsVersionWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsDebugWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsDebugWhenLongOptionGiven, this.description("testIsDebugWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsDebugWhenShortgOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsDebugWhenShortgOptionGiven, this.description("testIsDebugWhenShortgOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsCacheDisabledWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsCacheDisabledWhenLongOptionGiven, this.description("testIsCacheDisabledWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsCacheDisabledWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsCacheDisabledWhenShortOptionGiven, this.description("testIsCacheDisabledWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsAdminPortalDisabledWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsAdminPortalDisabledWhenLongOptionGiven, this.description("testIsAdminPortalDisabledWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsAdminPortalDisabledWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsAdminPortalDisabledWhenShortOptionGiven, this.description("testIsAdminPortalDisabledWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsSslDisabledWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsSslDisabledWhenLongOptionGiven, this.description("testIsSslDisabledWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsSslDisabledWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsSslDisabledWhenShortOptionGiven, this.description("testIsSslDisabledWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsMuteWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsMuteWhenShortOptionGiven, this.description("testIsMuteWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsMuteWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsMuteWhenLongOptionGiven, this.description("testIsMuteWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsYamlProvidedWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsYamlProvidedWhenShortOptionGiven, this.description("testIsYamlProvidedWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsYamlProvidedWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsYamlProvidedWhenLongOptionGiven, this.description("testIsYamlProvidedWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testtHasAdminPortWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testtHasAdminPortWhenShortOptionGiven, this.description("testtHasAdminPortWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasAdminPortWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasAdminPortWhenLongOptionGiven, this.description("testHasAdminPortWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testtHasStubsPortWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testtHasStubsPortWhenShortOptionGiven, this.description("testtHasStubsPortWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testtHasStubsPortWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testtHasStubsPortWhenLongOptionGiven, this.description("testtHasStubsPortWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testtIsWatchWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testtIsWatchWhenShortOptionGiven, this.description("testtIsWatchWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testIsWatchWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testIsWatchWhenLongOptionGiven, this.description("testIsWatchWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testtHasKeystoreLocationWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testtHasKeystoreLocationWhenShortOptionGiven, this.description("testtHasKeystoreLocationWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasKeystoreLocationWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasKeystoreLocationWhenLongOptionGiven, this.description("testHasKeystoreLocationWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testtHasLocationWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testtHasLocationWhenShortOptionGiven, this.description("testtHasLocationWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasLocationWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasLocationWhenLongOptionGiven, this.description("testHasLocationWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasPasswordWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasPasswordWhenShortOptionGiven, this.description("testHasPasswordWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasPasswordWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasPasswordWhenLongOptionGiven, this.description("testHasPasswordWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasSslPortWhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasSslPortWhenShortOptionGiven, this.description("testHasSslPortWhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_testHasSslPortWhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::testHasSslPortWhenLongOptionGiven, this.description("testHasSslPortWhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_enabledTlsWithAlpnHttp2WhenShortOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::enabledTlsWithAlpnHttp2WhenShortOptionGiven, this.description("enabledTlsWithAlpnHttp2WhenShortOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_enabledTlsWithAlpnHttp2WhenLongOptionGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::enabledTlsWithAlpnHttp2WhenLongOptionGiven, this.description("enabledTlsWithAlpnHttp2WhenLongOptionGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldRemoveOptionTlsWithAlpnHttp2WhenDisableSslGiven() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldRemoveOptionTlsWithAlpnHttp2WhenDisableSslGiven, this.description("shouldRemoveOptionTlsWithAlpnHttp2WhenDisableSslGiven"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldBeFalseThatYamlIsNotProvided() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldBeFalseThatYamlIsNotProvided, this.description("shouldBeFalseThatYamlIsNotProvided"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailOnInvalidCommandlineLongOptionString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailOnInvalidCommandlineLongOptionString, this.description("shouldFailOnInvalidCommandlineLongOptionString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailOnInvalidCommandlineShortOptionString() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailOnInvalidCommandlineShortOptionString, this.description("shouldFailOnInvalidCommandlineShortOptionString"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailOnMissingArgumentForExistingShortOption() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailOnMissingArgumentForExistingShortOption, this.description("shouldFailOnMissingArgumentForExistingShortOption"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldFailOnMissingArgumentForExistingLongOption() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldFailOnMissingArgumentForExistingLongOption, this.description("shouldFailOnMissingArgumentForExistingLongOption"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnOneCommandlineParamWhenHelpArgPresent() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnOneCommandlineParamWhenHelpArgPresent, this.description("shouldReturnOneCommandlineParamWhenHelpArgPresent"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnEmptyCommandlineParams() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnEmptyCommandlineParams, this.description("shouldReturnEmptyCommandlineParams"));
        }

        @org.openjdk.jmh.annotations.Benchmark
        public void benchmark_shouldReturnCommandlineParams() throws java.lang.Throwable {
            this.createImplementation();
            this.runBenchmark(this.implementation()::shouldReturnCommandlineParams, this.description("shouldReturnCommandlineParams"));
        }

        @java.lang.Override
        public org.junit.runners.model.Statement applyRuleFields(org.junit.runners.model.Statement statement, org.junit.runner.Description description) {
            statement = this.applyRule(this.implementation().expectedException, statement, description);
            statement = super.applyRuleFields(statement, description);
            return statement;
        }

        private CommandLineInterpreterTest implementation;

        @java.lang.Override
        public void createImplementation() throws java.lang.Throwable {
            this.implementation = new CommandLineInterpreterTest();
        }

        @java.lang.Override
        public CommandLineInterpreterTest implementation() {
            return this.implementation;
        }
    }
}
