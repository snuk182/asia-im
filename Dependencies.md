#Build dependencies.

This project depends on [Smack project](http://www.igniterealtime.org/projects/smack/)'s functionality (XMPP/Jabber). You have to get an Android-patched Smack library copy from [here](http://code.google.com/p/asmack/) or made your own patch for fresh general Smack sources.

Note - as of [properties including trick](http://code.google.com/p/android/issues/detail?id=10076) the Smack's default extension manager won't work, and this will mirror in the lack of some XMPP protocol functionality (pictures loading, typing notifications etc). Please patch this also, either with filling desired extension references into the code, or in any other way.