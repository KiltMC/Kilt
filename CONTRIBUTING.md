# Contribution Guidelines

## Generative AI
No generative AI ("vibe coding") may be used for writing code within Kilt's codebase. It may be used to assist
(i.e. explaining concepts to you), but the code has to come from you. You may not use generative AI
in creating pull requests either, even if you may not understand English, you may write the information
for your pull request in your native language instead.

## Mixins

### `@Overwrite`
When writing mixin code for Kilt, you must **avoid `@Overwrite` mixins wherever possible.**

Only use `@Overwrite` when it is genuinely impossible to accomplish what you want to port over, and
even then, try your best to come up with an alternative solution.

Usually, [MixinExtras](https://github.com/LlamaLad7/MixinExtras/wiki) provides additional mixin injection
points that would allow for most procedures. Additionally, Kilt provides its [own mixin extensions](src/main/java/xyz/bluspring/kilt/helpers/mixin)
to help with additional mixin operations that would be useful in Kilt.

At the moment, Kilt only has 6 `@Overwrite` calls, and 2 of them seem like they could be replaced
with a custom mixin extension.

### `@Redirect`
Where possible, try to use `@WrapOperation` instead for better mod compatibility. If not, you are
well able to use `@Redirect`. The mixin requirements for `@Redirect` are much more loose compared
to `@Overwrite`, but still slightly strict.

When it comes to implementing patches that are effectively `@Redirect` calls, use `KiltHelper.INSTANCE.hasMethodOverride`
to make the patch a `@WrapOperation` instead, allowing both paths to still go through.

There are still many `@Redirect` calls in Kilt that I need to rewrite as `@WrapOperation` calls.