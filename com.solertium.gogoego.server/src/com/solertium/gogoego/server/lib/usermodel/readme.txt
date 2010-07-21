To use this usermodel package, one should add things as needed and...

Using ones existing "account" concept, extend PoliciedAccount
	-Override PoliciedAccount.matches() to verify this account's signature 
	against another.

Determine how to model the Resources used by the system
	-Override the Resource class, overriding the equals function if necessary
	 (See URIResource for an example)

	-Add rules to the PoliciedAccount's knowledgeBases governing these resources.
	
Extend ResourceAccessor with a way to access resources
	-Override the functions fetchResource() and putResource(). See VFSResourceAccessor
	 for an example.