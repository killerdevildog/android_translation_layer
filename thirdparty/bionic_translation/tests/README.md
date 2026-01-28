# Tests

## general explanation

Instead of writing our own tests, we run the bionic testsuite. We don't necessarily need to pass
every single test, but for everything that we do intend to implement there should be corresponding
tests that can be used to verify we did so correctly.

### regression testing

In `expected_to_pass`, we collect the names of tests that currently work, so that we can test
for breakages in the CI.

The lists are split between glibc/musl, and further between x86_64/x86/aarch64. This reflects
the current state of things, but over time we would like to get to a point where every test
that passes passes in all environments.

### false negatives

There are definitely false negatives, caused by the test technically not only testing for what it
claims to test for, but also for whatever functions it just happens to use to carry out it's
testing. For example, many tests fail on musl due to missing fortified functions, even though
not all of those tests are actually supposed to be testing for their presence.

### false positives

It's possible that some of the tests we currently have listed as expected_to_pass shouldn't be.
After root causing a failure, you may come to the conclusion that the test should have simply
not been listed as passing, in which case we should remove it from the list.

### flaky tests

Some tests may not deterministically pass or fail. Keep this in mind in case you happen to discover
such.

### notes

As a result of using tests from the android 16 tag, we hit some "fun" issues that we won't need
to deal with for a while when running real world apps which generally target older android versions
and can't afford to enable all the shiny new features.

For example, on aarch64, Shadow Call Stack is enabled for the libcxx statically linked into the test
suite. By sheer luck, we are able to work around the issues this causes, other than the inability
to output an xml, because the crashes caused by the libcxx assuming everything in the whole process
was compiled with `--ffixed-18` seem to mainly happen in destructors, which we can skip with a hack.

## running tests locally

First, make sure the submodule is checked out.

Then, from your builddir:

`LINKER_IGNORE_UNKNOWN_RELOCS= BUILDDIR_PATH=$PWD time ../tests/_run_in_bwrap.sh tests/test_runner ../tests/CtsBionicTestCases/ [gtest options, e.g --help]`

Add `GDB=1` to the envs if you wish to run under gdb. (note: make sure to only specify a single test
in the filter when running under gdb)

### what to do when you're trying to make an app work in ATL

- first, just run `meson test` to in case your environment exposes some tests as false positives
(there is e.g a test which checks that the size limit for core dump size is non-zero, which is not
really testing for anything we care about and is not guaranteed to be so on every system)

- If you have a vague idea of what may be causing issues for you, try to grep `--gtest_list_tests`
for related tests, and then use `--gtest_filter` to run said tests. If any fails, check that it's
not a false negative (if a function is missing, may need to fix that and goto 10), and if not, check
for known differences between that function's behavior in glibc/musl and bionic. If we already have
a wrapper, it may be implemented incorrectly (either in general or it just fails to account for some
difference between glibc/musl or 64/32 bit or x86/aarch64).

- If you have successfully fixed something and now tests pass which didn't pass before, add them
to `expected_to_pass/common.txt`. Run `meson test` to check for regressions. If you can't trivially
check in other environments, just make the MR and let the CI do that for you.
