<?xml version="1.0" ?>
<!-- this is an XSLT 1.0 stylesheet -->
<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xt="http://www.oxygenxml.com/ns/extension"
	version="1.0">
	<xsl:output method="text"/>

	<xsl:template match="/">
		<xsl:value-of select="concat('tei-package-location=', /xt:extensions/xt:extension[last()]/xt:location/@href)"/>
	</xsl:template>

</xsl:stylesheet>
