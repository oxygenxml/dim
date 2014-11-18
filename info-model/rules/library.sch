<?xml version="1.0" encoding="UTF-8"?>
<schema xmlns="http://purl.oclc.org/dsdl/schematron"
  queryBinding="xslt2">
  
  <pattern abstract="true" id="avoidWordInElement">
    <title>Issue a warning if a word appears inside a specified
      element</title>
    <p>This pattern allows you to advise users not to use a specific
      word in an element.</p>
    <p>As parameters we have <emph>word</emph> that points to the word
      that we need to check, <emph>element</emph> that points to the
      element we will check to not contain that word and
      <emph>message</emph> that contains the message we should display
      to the user in case the word appears in the specified element.</p>
    <parameters xmlns="http://oxygenxml.com/ns/schematron/params">
      <parameter>
        <name>element</name>
        <desc>Specifies the element we will verify to not contain a specified word.</desc>
      </parameter>
      <parameter>
        <name>word</name>
        <desc>Specifies the word we will look for.</desc>
      </parameter>
      <parameter>
        <name>message</name>
        <desc>The message the end user will see when the specified word appears
          in the specified element.</desc>
      </parameter>
    </parameters> 
    <rule context="$element">
      <assert test="not(tokenize(normalize-space(.), ' ') = '$word')"
        role="warn">
        <value-of select="'$message'"/>
      </assert>
    </rule>
  </pattern>
  
  <pattern abstract="true" id="avoidEndFragment">
    <title>Issue a warning if a an element end with a specified fragment
      or character</title>
    <p>This pattern allows you to advise users not to use a specific end
      sequence to end an element.</p>
    <p>As parameters we have <emph>fragment</emph> that points to the
      text that we need to check, <emph>element</emph> that points to
      the element we will check to not end with that fragment and
      <emph>message</emph> that contains the message we should display
      to the user in case the fragment appears at the end of the the
      specified element.</p>
    <parameters xmlns="http://oxygenxml.com/ns/schematron/params">
      <parameter>
        <name>element</name>
        <desc>Specifies the element we will verify to not contain a specified word.</desc>
      </parameter>
      <parameter>
        <name>fragment</name>
        <desc>Specifies the text to check.</desc>
      </parameter>
      <parameter>
        <name>message</name>
        <desc>The message the end user will see when the specified text ends with the given fragment.</desc>
      </parameter>
    </parameters> 
    <rule context="$element">
      <assert test="not(ends-with(normalize-space(.), '$fragment'))"
        role="warn">
        <value-of select="'$message'"/>
      </assert>
    </rule>
  </pattern>
  
  <pattern abstract="true" id="avoidAttributeInElement">
    <title>Issue a warning if an attribute appears inside a specified
      element</title>
    <p>This pattern allows you to advise users not to use a specific
      attribute in an element.</p>
    <p>As parameters we have <emph>attribute</emph> that points to the
      attribute that we need to check, <emph>element</emph> that points
      to the element we will check to not contain that attribute and
      <emph>message</emph> that contains the message we should display
      to the user in case the attribute appears in the specified
      element.</p>
    <parameters xmlns="http://oxygenxml.com/ns/schematron/params">
      <parameter>
        <name>element</name>
        <desc>Specifies the element we will verify to not contain a specified word.</desc>
      </parameter>
      <parameter>
        <name>attribute</name>
        <desc>Specifies the forbidden attribute.</desc>
      </parameter>
      <parameter>
        <name>message</name>
        <desc>The message the end user will see when the forbidden attribute is encountered.</desc>
      </parameter>
    </parameters> 
    <rule context="$element">
      <assert test="not(@$attribute)" role="warn">
        <value-of select="'$message'"/>
      </assert>
    </rule>
  </pattern>
  
  <pattern id="recommendElementInParent" abstract="true">
    <title>Recommend usage of an element inside a parent element</title>
    <p>This pattern allows you to advise users to enter a specific
      element inside a parent, usually that element is optional but we
      want to advise users to use it.</p>
    <p>As parameters we have <emph>parent</emph> that points to the
      parent element, <emph>element</emph> that points to the child
      element and <emph>message</emph> that contains the message we
      should display to the user in case the element is not present
      within the parent element.</p>
    <parameters xmlns="http://oxygenxml.com/ns/schematron/params">
      <parameter>
        <name>parent</name>
        <desc>Specifies the parent element.</desc>
      </parameter>
      <parameter>
        <name>element</name>
        <desc>Specifies the element that should appear in the parent.</desc>
      </parameter>
      <parameter>
        <name>message</name>
        <desc>The message the end user will see when recommended child is not found in the parent.</desc>
      </parameter>
    </parameters> 
    <rule context="$parent">
      <assert test="$element" role="warn">
        <value-of select="'$message'"/>
      </assert>
    </rule>
  </pattern>
  
  <pattern id="restrictWords" abstract="true">
    <title>Check the number of words to be within certain limits</title>
    <p>This pattern allows to check that the number of words in an
      element fits between a lower and an upper limit and instructs the
      user to stay within thse limits.</p>
    <p>As parameters we have <emph>parentElement</emph> that specifies
      the element containing the text to be checked,
      <emph>minWords</emph> and <emph>maxWords</emph> that specify the
      minimum and maximum number of words, respectively.</p>
    <parameters xmlns="http://oxygenxml.com/ns/schematron/params">
      <parameter>
        <name>parentElement</name>
        <desc>Specifies the element who's word number should be counted.</desc>
      </parameter>
      <parameter>
        <name>minWords</name>
        <desc>Specifies the minimum number of words that is accepted.</desc>
      </parameter>
      <parameter>
        <name>maxWords</name>
        <desc>Specifies the maximum number of words that is accepted.</desc>
      </parameter>
    </parameters> 
    <rule context="$parentElement">
      <let name="words" value="count(tokenize(normalize-space(.), ' '))"/>
      <assert test="$words &lt;= $maxWords" role="warn"> It is
        recommended to not exceed <value-of select="'$maxWords '"/>
        words! You have <value-of select="$words"/>
        <value-of select="if ($words=1) then ' word' else ' words'"/>. </assert>
      <assert test="$words &gt;= $minWords" role="warn"> It is
        recommended to have at least <value-of select="'$minWords '"/>
        words! You have <value-of select="$words"/>
        <value-of select="if ($words=1) then ' word' else ' words'"/>.
      </assert>
    </rule>
  </pattern>
  
</schema>
