<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    xmlns:sch="http://purl.oclc.org/dsdl/schematron"
    version="2.0">
    
    <xsl:template match="sch:schema">
        <rules>
            <xsl:apply-templates/>
        </rules>
    </xsl:template>
    <xsl:template match="sch:pattern[@abstract='true']">
        <xsl:element name="rule">
            <xsl:attribute name="name" select="@id"/>
            <xsl:element name="doc">
                <html>
                    <body>
                        <xsl:apply-templates select="sch:title | sch:p" mode="convert-to-html"/>
                    </body>
                </html>
            </xsl:element>
            
            <xsl:copy-of select="*:parameters" copy-namespaces="no"/>
            
            <!-- 
            <xsl:variable name="lets" select=".//sch:let/@name"></xsl:variable>
            
            <xsl:for-each select="sch:rule/@context | sch:rule/sch:assert/@test | sch:rule/sch:report/@test">
                <xsl:variable name="params" select="oxyUtil:extract-params(xs:string(.))"/>
                <xsl:for-each select="distinct-values($params[not(.=$lets)])">
                    <param><xsl:value-of select="."/></param>
                </xsl:for-each>
            </xsl:for-each>
            
            -->
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="text()"/>
    
    
    <!-- Deep copy template -->
    <xsl:template match="*|text()|@*" mode="convert-to-html">
        <xsl:copy>
            <xsl:apply-templates mode="convert-to-html" select="@*"/>
            <xsl:apply-templates mode="convert-to-html"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*:emph" mode="convert-to-html">
        <xsl:element name="b">
            <xsl:apply-templates mode="convert-to-html" select="@*"/>
            <xsl:apply-templates mode="convert-to-html"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="*:title" mode="convert-to-html">
        <xsl:element name="h2">
            <xsl:apply-templates mode="convert-to-html" select="@*"/>
            <xsl:apply-templates mode="convert-to-html"/>
        </xsl:element>
    </xsl:template>
    
</xsl:stylesheet>