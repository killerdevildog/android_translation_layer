#include <math.h>

#define BIT(x) (1 << x)

#define	BIONIC_FP_INFINITE  BIT(0)
#define	BIONIC_FP_NAN       BIT(1)
#define	BIONIC_FP_NORMAL    BIT(2)
#define	BIONIC_FP_SUBNORMAL BIT(3)
#define	BIONIC_FP_ZERO      BIT(4)

#define bionic_fpclassify(x) __builtin_fpclassify(BIONIC_FP_NAN, \
                                                  BIONIC_FP_INFINITE, \
                                                  BIONIC_FP_NORMAL, \
                                                  BIONIC_FP_SUBNORMAL, \
                                                  BIONIC_FP_ZERO, \
                                                  x)

/* on x86(_64), androideabi uses different long double ABI */
#if defined(__i386__)
	typedef double b_long_double;
#elif defined (__x86_64__)
	typedef __float128 b_long_double;
#else
	typedef long double b_long_double;
#endif

int bionic_isnan(double val) {
	return isnan(val);
}

int bionic___fpclassify(double d)
{
	return bionic_fpclassify(d);
}
int bionic___fpclassifyf(float f)
{
	return bionic_fpclassify(f);
}
int bionic___fpclassifyd(double d)
{
	return bionic_fpclassify(d);
}
int bionic___fpclassifyl(b_long_double e)
{
	return bionic_fpclassify(e);
}

#if defined(__GLIBC__) && defined(__aarch64__)
/* glibc raises the exception naturally, musl and bionic don't; use the musl implementation */
asm (
	".global feraiseexcept         \n"
	".type feraiseexcept,%function \n"
	"	feraiseexcept:         \n"
	"	and w0, w0, #0x1f      \n"
	"	mrs x1, fpsr           \n"
	"	orr w1, w1, w0         \n"
	"	msr fpsr, x1           \n"
	"	mov w0, #0             \n"
	"	ret                    \n"
);
#endif

/* these long double functions are not bionic-exclusive, but they are incompatible on x86(_64) */

#if defined(__i386__) || defined (__x86_64__)

b_long_double bionic_acosl(b_long_double x)
{
	return acosl(x);
}

b_long_double bionic_asinl(b_long_double x)
{
	return asinl(x);
}

b_long_double bionic_atanl(b_long_double x)
{
	return atanl(x);
}

b_long_double bionic_atan2l(b_long_double y, b_long_double x)
{
	return atan2l(x, y);
}

b_long_double bionic_cosl(b_long_double x)
{
	return cosl(x);
}

b_long_double bionic_sinl(b_long_double x)
{
	return sinl(x);
}

b_long_double bionic_tanl(b_long_double x)
{
	return tanl(x);
}

b_long_double bionic_acoshl(b_long_double x)
{
	return acoshl(x);
}

b_long_double bionic_asinhl(b_long_double x)
{
	return asinhl(x);
}

b_long_double bionic_atanhl(b_long_double x)
{
	return atanhl(x);
}

b_long_double bionic_coshl(b_long_double x)
{
	return coshl(x);
}

b_long_double bionic_sinhl(b_long_double x)
{
	return sinhl(x);
}

b_long_double bionic_tanhl(b_long_double x)
{
	return tanhl(x);
}

b_long_double bionic_expl(b_long_double x)
{
	return expl(x);
}

b_long_double bionic_exp2l(b_long_double x)
{
	return exp2l(x);
}

b_long_double bionic_expm1l(b_long_double x)
{
	return expm1l(x);
}

b_long_double bionic_frexpl(b_long_double x, int* exponent)
{
	return frexpl(x, exponent);
}

int bionic_ilogbl(b_long_double x)
{
	return ilogbl(x);
}

b_long_double bionic_ldexpl(b_long_double x, int exponent)
{
	return ldexpl(x, exponent);
}

b_long_double bionic_logl(b_long_double x)
{
	return logl(x);
}

b_long_double bionic_log10l(b_long_double x)
{
	return log10l(x);
}

b_long_double bionic_log1pl(b_long_double x)
{
	return log1pl(x);
}

b_long_double bionic_log2l(b_long_double x)
{
	return log2l(x);
}

b_long_double bionic_logbl(b_long_double x)
{
	return logbl(x);
}

b_long_double bionic_modfl(b_long_double x, b_long_double* integral_part)
{
	long double _integral_part = *integral_part;
	int ret = modfl(x, &_integral_part);
	*integral_part = _integral_part;
	return ret;
}

b_long_double bionic_scalbnl(b_long_double x, int exponent)
{
	return scalbnl(x, exponent);
}

b_long_double bionic_scalblnl(b_long_double x, long exponent)
{
	return scalblnl(x, exponent);
}

b_long_double bionic_cbrtl(b_long_double x)
{
	return cbrtl(x);
}

b_long_double bionic_fabsl(b_long_double x)
{
	return fabsl(x);
}

b_long_double bionic_hypotl(b_long_double x, b_long_double y)
{
	return hypotl(x, y);
}

b_long_double bionic_powl(b_long_double x, b_long_double y)
{
	return powl(x, y);
}

b_long_double bionic_sqrtl(b_long_double x)
{
	return sqrtl(x);
}

b_long_double bionic_erfl(b_long_double x)
{
	return erfl(x);
}

b_long_double bionic_erfcl(b_long_double x)
{
	return erfcl(x);
}

b_long_double bionic_lgammal(b_long_double x)
{
	return lgammal(x);
}

b_long_double bionic_tgammal(b_long_double x)
{
	return tgammal(x);
}

b_long_double bionic_ceill(b_long_double x)
{
	return ceill(x);
}

b_long_double bionic_floorl(b_long_double x)
{
	return floorl(x);
}

b_long_double bionic_nearbyintl(b_long_double x)
{
	return nearbyintl(x);
}

b_long_double bionic_rintl(b_long_double x)
{
	return rintl(x);
}

long bionic_lrintl(b_long_double x)
{
	return lrintl(x);
}

long long bionic_llrintl(b_long_double x)
{
	return llrintl(x);
}

b_long_double bionic_roundl(b_long_double x)
{
	return roundl(x);
}

long bionic_lroundl(b_long_double x)
{
	return lroundl(x);
}

long long bionic_llroundl(b_long_double x)
{
	return llroundl(x);
}

b_long_double bionic_truncl(b_long_double x)
{
	return truncl(x);
}

b_long_double bionic_fmodl(b_long_double x, b_long_double y)
{
	return fmodl(x, y);
}

b_long_double bionic_remainderl(b_long_double x, b_long_double y)
{
	return remainderl(x, y);
}

b_long_double bionic_remquol(b_long_double x, b_long_double y, int* quotient_bits)
{
	return remquol(x, y, quotient_bits);
}

b_long_double bionic_copysignl(b_long_double value, b_long_double sign)
{
	return copysignl(value, sign);
}

b_long_double bionic_nanl(const char* kind)
{
	return nanl(kind);
}

b_long_double bionic_nextafterl(b_long_double x, b_long_double y)
{
	return nextafterl(x, y);
}

double bionic_nexttoward(double x, b_long_double y)
{
	return nexttoward(x, y);
}

float bionic_nexttowardf(float x, b_long_double y)
{
	return nexttowardf(x, y);
}

b_long_double bionic_nexttowardl(b_long_double x, b_long_double y)
{
	return nexttowardl(x, y);
}

b_long_double bionic_fdiml(b_long_double x, b_long_double y)
{
	return fdiml(x, y);
}

b_long_double bionic_fmaxl(b_long_double x, b_long_double y)
{
	return fmaxl(x, y);
}

b_long_double bionic_fminl(b_long_double x, b_long_double y)
{
	return fminl(x, y);
}

b_long_double bionic_fmal(b_long_double x, b_long_double y, b_long_double z)
{
	return fmal(x, y, z);
}

b_long_double bionic_lgammal_r(b_long_double x, int* sign)
{
	return lgammal_r(x, sign);
}

b_long_double bionic_significandl(b_long_double x)
{
	/* glibc has this, bionic has it for compatibility with glibc; musl had it but removed it */
#ifdef __GLIBC__
	return significandl(x);
#else
	return scalbnl(x, -ilogbl(x));
#endif
}

void bionic_sincosl(b_long_double x, b_long_double* sin, b_long_double* cos)
{
	long double _sin = *sin;
	long double _cos = *cos;
	sincosl(x, &_sin, &_cos);
	*sin = _sin;
	*cos = _cos;
}

#endif
