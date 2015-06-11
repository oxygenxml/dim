Dynamic Information Model (DIM)
===============================

The DIM project is an implementation of an intelligent style guide that not only
describes the rules the content should follow but can also enforce those rules. 
Another goal is to be auto-discoverable, style guide topics being annotated with
meta information that allows this.

Description
-----------

The intelligent style guide from the DIM project is implemented in DITA. Rules 
to enforce the style guide prose are encoded using existing DITA markup, we use
a data list (`dl`) element placed into a `section` marked with the `audience` 
attribute set to `styleguide`.
These embedded rules are instantiations of generic rules defined in a library. 
To instantiate a rule, we just need to specify the rule name and values for its
parameters.  

To make the style guide discoverable, we annotate topics with meta information 
that described what element or attribute use that topic is about. Thus, this 
information can be used to point the user back to that topic when the user is
interested in an element or attribute that topic describes.   

Content
-------

The project contains: 

* Sample style guide that can serve as a starting point for
your own style guide - in case you do not have one already. This was contributed
by [Comtech Services](http://www.comtech-serv.com) 

* Library of generic rules, defined as Schematorn abstract patters

* XSLT script that will generate a Schematron file that contains all the rules 
defined in the style guide topics, each with a link that points back so the 
style guide topic where the rule was defined
   
* XSLT script that will generate an oXygen configuration file that will make the
style guide topics discoverable from the documentation tool tips oXygen presents
for elements and attributes

* oXygen framework that extends DITA support to provide DIM specific actions, 
that allow to easily define new rules and to annotate topics with meta 
information

Getting started and deliverables
--------------------------------

To get started, clone the DIM project and either use the provided style guide as 
a starting point, or replace that with your own style guide.

If you open a style guide topic in oXygen make sure you enable the `+ DIM` style 
from the *Styles* drop down to see the DIM specific actions. Use the provided 
DIM specific actions to annotate topics with meta information and to add rules.
 
Publish the style guide using the oXygen WebHelp transformation into the 
`info-model/styleguide` folder. This can be easily done in oXygen using the 
pre-configured `Generate Style Guide` transformation scenario.

Run the `gen-rules/gen-rules.xsl` and `gen-rules/gen-mappings.xsl` on the style
guide DITA map in order to generate the `rules.sch` Schematron file and the 
`contentCompletionElementsMap.xml` configuration file.

The generated style guide output, the schematron file and the configuration file
are the deliverables of this project. You can use them to provide the integrated
style guide experience to your authors in your real project.

Setting up oXygen to use the style guide
----------------------------------------
To be able to use the intelligent style guide in oXygen, we need to use the 
generated Schematron schema to validate the documents we want to enforce the 
style guide rules on, we need to make the published style guide form accessible 
and to setup the configuration file so that elements and attribute tool tips 
will point to the style guide topics that describe their usage. 

*   Associate Schematron schema with files
    
    A Schematron schema can be associated with an XML document either within the 
    document itself, using an `xml-model` processing instruction, or it can be 
    associated externally, at the application level.
    
    A Schematron validation can be set outside the document, at the framework 
    level. Each framework configuration has a `Validation` tab where you can 
    specify a validation scenario. If no scenario is available, create one 
    containing a validation unit that validates the document with the detected 
    schemas. Then add a new validation unit that validates the document with the
    generated Schematron schema and depending on your preference, you can add 
    that to be part of the automatic validation, or just leave it to be applied
    only when the authors trigger the validation action themselves, using the 
    validate action.

*   Make the published style guide accessible
    
    You can place the generated WebHelp form of the style guide either locally 
    or you may publish that on a web server. In the later case you may consider
    also generating Feedback-enabled WebHelp, rather than simple WebHelp, to
    allow authors to send you feedback on the style guide content.
    
    Then, you need to edit the `info-model/rules/catalog.xml` file to update the
    `rewritePrefix` value to point to the base location of your published style 
    guide
    
    `<rewriteURI uriStartString="http://example.com/styleguide/webhelp/" 
    rewritePrefix="styleguide/webhelp/"/>`
    
*   Setup oXygen to show tool tips for elements and attributes
    
    This setting can be done as part of a framework, like the DITA framework for
    example, which is used to provide DITA editing support in oXygen. Let's say 
    you customized the DITA framework by extending it in a framework named
    `dita-my-project`. Then you need to edit the catalog file to update the 
    `http://www.oxygenxml.com/dita_dim/styleguide/contentCompletionElementsMap.xml`
    in    
    
    `<uri name="http://www.oxygenxml.com/dita/styleguide/contentCompletionElementsMap.xml" 
    uri="contentCompletionElementsMap.xml"/>`

    to replace `dita` with `dita-my-project`.
    
    For eaxh framework, oXygen will try to load this configuration file from 
    such an URL, and if that is mapped by the catalog to some actual 
    configuration file, then that will be automatically used to provide links to
    style guide topics from the element and attribute tooltips. 

    Note
    --
    These deliverables can be used also with other tools, the style guide can be 
    published to other formats and you can generate different tools 
    configuration file and then set everything up as you want. The DIM project 
    is about generic ideas but it provides a specific implementation using DITA
    and oXygen. 

License
-------

The DIM project is licensed for use under the 
[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

Copyright © 2015, Syncro Soft SRL 





 


