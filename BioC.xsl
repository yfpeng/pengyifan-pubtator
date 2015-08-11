<?xml version="1.0" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <head>
        <style>
          body {
          margin: auto;
          font-size: 12pt;
          font-family: 'Open Sans', Arial, sans-serif;
          width: 1010px;
          }

          h1 {
          font-size: 12pt;
          }

          h2 {
          font-size: 1em;
          padding: 0;
          margin: 0;
          }

          table {
          background: #f5f5f5;
          border-collapse: collapse;
          box-shadow: inset 0 1px 0 #fff;
          font-size: 12px;
          line-height: 24px;
          margin: 30px auto;
          text-align: left;
          width: 500px;
          }

          th, td {
          border: 1px solid black;
          padding: 0 4px;
          text-align: left;
          }

          th {
          background: url(http://jackrugile.com/images/misc/noise-diagonal.png), linear-gradient(#777, #444);
          border-left: 1px solid #555;
          border-right: 1px solid #777;
          border-top: 1px solid #555;
          border-bottom: 1px solid #333;
          box-shadow: inset 0 1px 0 #999;
          color: #fff;
          font-weight: bold;
          position: relative;
          text-shadow: 0 1px 0 #000;
          }

          tr.disease, td.disease {
          color: #8B4513;
          }

          tr.chemical, td.chemical {
          color: #006400;
          }

          .right-align {
          text-align: right;
          }

          .ner-table {
          padding: 0;
          margin: 0;
          width: 30vw;
          <!--border: 1px solid blue;-->
          display: table-cell;
          }

          .cid-table {
          padding: 0;
          margin: 0;
          width: 30vw;
          <!--border: 1px solid blue;-->
          display: table-cell;
          }

          .clear {
          clear: both;
          }
        </style>
      </head>
      <body>
        <xsl:apply-templates select="collection/document"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="document">
    <h1>
      <xsl:value-of select="id"/>: <xsl:value-of select="passage[1]/text"/>
    </h1>
    <p>
        <xsl:value-of select="passage[2]/text"/>
      </p>
    <div class="ner-table">
      <table>
        <tr>
          <th>Type</th>
          <th>ID</th>
          <th class="right-align">Offset</th>
          <th class="right-align">Length</th>
          <th>Text</th>
        </tr>
        <xsl:for-each select="passage/annotation">
          <xsl:variable name="tr-class">
            <xsl:choose>
              <xsl:when test="infon[@key='type']='Disease'">disease</xsl:when>
              <xsl:when test="infon[@key='type']='Chemical'">chemical</xsl:when>
              <xsl:otherwise>other</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>

          <tr class="{$tr-class}">
            <td>
              <xsl:value-of select="infon[@key='type']"/>
            </td>
            <td>
              <xsl:value-of select="infon[@key='MESH']"/>
            </td>
            <td class="right-align">
              <xsl:value-of select="location/@offset"/>
            </td>
            <td class="right-align">
              <xsl:value-of select="location/@length"/>
            </td>
            <td>
              <xsl:value-of select="text"/>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
    <div class="cid-table">
      <table>
        <tr>
          <th>Type</th>
          <th>Chemical</th>
          <th>Disease</th>
        </tr>
        <xsl:for-each select="relation">
          <tr>
            <td>
              <xsl:value-of select="infon[@key='relation']"/>
            </td>
            <td class="disease">
              <xsl:value-of select="infon[@key='Chemical']"/>
            </td>
            <td class="chemical">
              <xsl:value-of select="infon[@key='Disease']"/>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
    <div class="clear"></div>
  </xsl:template>
</xsl:stylesheet>