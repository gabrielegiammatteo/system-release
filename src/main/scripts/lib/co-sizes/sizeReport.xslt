<xsl:transform
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:preserve-space elements="td" />

<xsl:template match="/">
    <html>
    <head>
    	<title>D4Science Size Statistics</title>
    	<style>
    		th {
    			background:lightgray;
    		}
    		td {
    			white-space:nowrap;
    			font-family:tahoma;
    			font-size:0.8em;
    			cursor:default;
    			border-bottom:1px solid lightgray;
    			border-right:1px solid #f0f0f0;
    		}
    		tr:hover {
    			background:#f0f0ff;
    		}
    		td.bad:hover {
    			background:#ff8000;
    		}
    		td.bad {
    			background:#fff0f0;
    		}
    	
    	</style>
    </head>
    <body>
    
    <center>
    
    <span style="font-size:2em">
		<b>D4Science - Size Statistics </b>
	</span>
    </center>
    <br/>
      
    <table width="100%" cellpadding="2" cellspacing="0">
    <tr>
    	<th>Module Name</th>
    	<th>Size</th>
    	<th>Files</th>
    	<th>.jar</th>
    	<th>.svn-base</th>
    	<th>.html</th>
    	<th>.zip</th>
    	<th>.class</th>
    	<th>.war</th>
    	<th>.gar</th>
    	<th>.gz</th>
    	<th>.tar</th>
    	<th>.so</th>
    	<th>.aut</th>
    	<th>.0</th>
    	<th>.2</th>
    	<th>.23</th>
    </tr>
    <xsl:apply-templates select="workspace"/>
    </table>
    </body>
    </html>

</xsl:template>

<xsl:template match="/workspace">

	<tr style="background:#e0e0ff; font-weight:bold">
		<td style="font-size:1.0em">
			<b>Summary</b>
		</td>
		<td style="text-align:center">
			<xsl:value-of select="@size"/>&#160;<xsl:value-of select="@unit"/>
		</td>
		<td style="text-align:center">
			<xsl:value-of select="@files"/> files
		</td>
		<xsl:choose>
			<xsl:when test="type[@name='jar']">
		       <xsl:apply-templates select="type[@name='jar']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='svn-base']">
		       <xsl:apply-templates select="type[@name='svn-base']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='html']">
		       <xsl:apply-templates select="type[@name='html']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='zip']">
		       <xsl:apply-templates select="type[@name='zip']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='class']">
		       <xsl:apply-templates select="type[@name='class']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='war']">
		       <xsl:apply-templates select="type[@name='war']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='gar']">
		       <xsl:apply-templates select="type[@name='gar']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='gz']">
		       <xsl:apply-templates select="type[@name='gz']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='tar']">
		       <xsl:apply-templates select="type[@name='tar']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='so']">
		       <xsl:apply-templates select="type[@name='so']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='aut']">
		       <xsl:apply-templates select="type[@name='aut']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='0']">
		       <xsl:apply-templates select="type[@name='0']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='2']">
		       <xsl:apply-templates select="type[@name='2']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='23']">
		       <xsl:apply-templates select="type[@name='23']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
    </tr>
    <xsl:for-each select="module">
		<xsl:sort select="@bytes" order="descending" data-type="number"/>
		<xsl:sort select="@name" order="ascending" data-type="text"/>
	    <xsl:apply-templates select="."/>
    </xsl:for-each>
</xsl:template>

<xsl:template match="module">
	<tr>
		<td>
			<b><xsl:value-of select="@name"/></b>
		</td>
		<td style="text-align:center">
			<!--xsl:value-of select="@bytes"/-->
			<xsl:value-of select="@size"/>&#160;<xsl:value-of select="@unit"/>
		</td>
		<td style="text-align:center">
			<xsl:value-of select="@files"/> files
		</td>
		<xsl:choose>
			<xsl:when test="type[@name='jar']">
		       <xsl:apply-templates select="type[@name='jar']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='svn-base']">
		       <xsl:apply-templates select="type[@name='svn-base']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='html']">
		       <xsl:apply-templates select="type[@name='html']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>		
		<xsl:choose>
			<xsl:when test="type[@name='zip']">
		       <xsl:apply-templates select="type[@name='zip']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='class']">
		       <xsl:apply-templates select="type[@name='class']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='war']">
		       <xsl:apply-templates select="type[@name='war']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='gar']">
		       <xsl:apply-templates select="type[@name='gar']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='gz']">
		       <xsl:apply-templates select="type[@name='gz']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='tar']">
		       <xsl:apply-templates select="type[@name='tar']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='so']">
		       <xsl:apply-templates select="type[@name='so']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='aut']">
		       <xsl:apply-templates select="type[@name='aut']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='0']">
		       <xsl:apply-templates select="type[@name='0']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='2']">
		       <xsl:apply-templates select="type[@name='2']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose>
			<xsl:when test="type[@name='23']">
		       <xsl:apply-templates select="type[@name='23']"/>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>

    </tr>
</xsl:template>

<xsl:template match="type">
	<td style="text-align:center" class="bad">
		<b><xsl:value-of select="@size"/>&#160;<xsl:value-of select="@unit"/></b>&#160;<br/><small>(.<xsl:value-of select="@name"/>)</small>
	</td>
</xsl:template>

</xsl:transform>
