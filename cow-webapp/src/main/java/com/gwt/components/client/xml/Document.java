/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

/*
Simple XML Parser for GWT
Copyright (C) 2006 musachy http://gwt.components.googlepages.com/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/

package com.gwt.components.client.xml;

import java.util.ArrayList;

import com.google.gwt.core.client.JavaScriptObject;

public class Document {

  private ArrayList childNodes = new ArrayList();
  public static final String DOM_ELEMENT_NODE = "DOM_ELEMENT_NODE";
  public static final String DOM_TEXT_NODE = "DOM_TEXT_NODE";

  Document() {
  }

  public static Document newDocument() {
    return new Document();
  }

  public Node createNode(String name) {
    return new Node(DOM_ELEMENT_NODE, name, null, this);
  }

  public Node createTextNode(String value) {
    return new Node(DOM_TEXT_NODE, "#text", value, this);
  }

  public void appendChild(Node child) {
    childNodes.add(child);
  }

  public static native String xmlResolveEntities(String s) /*-{
    var parts =
      @com.gwt.components.client.xml.Document::stringSplit(Ljava/lang/String;Ljava/lang/String;)(s,'&');
    var ret = parts[0];
    for (var i = 1; i < parts.length; ++i) {
      var rp =
        @com.gwt.components.client.xml.Document::stringSplit(Ljava/lang/String;Ljava/lang/String;)(parts[i],';');
      if (rp.length == 1) {
        // no entity reference: just a & but no ;
        ret += parts[i];
        continue;
      }

      var ch;
      switch (rp[0]) {
        case 'lt':
          ch = '<';
          break;
        case 'gt':
          ch = '>';
          break;
        case 'amp':
          ch = '&';
          break;
        case 'quot':
          ch = '"';
          break;
        case 'apos':
          ch = '\'';
          break;
        case 'nbsp':
          ch = String.fromCharCode(160);
          break;
        default:
          // Cool trick: let the DOM do the entity decoding. We assign
          // the entity text through non-W3C DOM properties and read it
          // through the W3C DOM. W3C DOM access is specified to resolve
          // entities.
          var span = $wnd.document.createElement('span');
          span.innerHTML = '&' + rp[0] + '; ';
          ch = span.childNodes[0].nodeValue.charAt(0);
      }
      ret += ch + rp[1];
    }
    return ret;
  }-*/;

  private static native JavaScriptObject stringSplit(String s, String c) /*-{
    var a = s.indexOf(c);
    if (a == -1) {
      return [ s ];
    }
    var parts = [];
    parts.push(s.substr(0,a));
    while (a != -1) {
      var a1 = s.indexOf(c, a + 1);
      if (a1 != -1) {
        parts.push(s.substr(a + 1, a1 - a - 1));
      } else {
        parts.push(s.substr(a + 1));
      }
      a = a1;
    }
    return parts;
  }-*/;

  public static native Document xmlParse(String xml) /*-{
    var regex_empty = /\/$/;

    // See also <http://www.w3.org/TR/REC-xml/#sec-common-syn> for
    // allowed chars in a tag and attribute name. TODO(mesch): the
    // following is still not completely correct.

    var regex_tagname = /^([\w:-]*)/;
    var regex_attribute = /([\w:-]+)\s?=\s?('([^\']*)'|"([^\"]*)")/g;

    var xmldoc = @com.gwt.components.client.xml.Document::newDocument()();
    var root = xmldoc;

    // For the record: in Safari, we would create native DOM nodes, but
    // in Opera that is not possible, because the DOM only allows HTML
    // element nodes to be created, so we have to do our own DOM nodes.

    // xmldoc = document.implementation.createDocument('','',null);
    // root = xmldoc; // .createDocumentFragment();
    // NOTE(mesch): using the DocumentFragment instead of the Document
    // crashes my Safari 1.2.4 (v125.12).
    var stack = [];

    var parent = root;
    stack.push(parent);

    var x =
      @com.gwt.components.client.xml.Document::stringSplit(Ljava/lang/String;Ljava/lang/String;)(xml, '<');
    for (var i = 1; i < x.length; ++i) {
      var xx =
        @com.gwt.components.client.xml.Document::stringSplit(Ljava/lang/String;Ljava/lang/String;)(x[i],'>');
      var tag = xx[0];
      var text =
        @com.gwt.components.client.xml.Document::xmlResolveEntities(Ljava/lang/String;)(xx[1] || '');

      if (tag.charAt(0) == '/') {
        stack.pop();
        parent = stack[stack.length-1];
      } else if (tag.charAt(0) == '?') {
        // Ignore XML declaration and processing instructions
      } else if (tag.charAt(0) == '!') {
        // Ignore notation and comments
      } else {
        var empty = tag.match(regex_empty);
        var tagname = regex_tagname.exec(tag)[1];
        var node =
          xmldoc.@com.gwt.components.client.xml.Document::createNode(Ljava/lang/String;)(tagname);

        var att;
        while (att = regex_attribute.exec(tag)) {
          var val =
            @com.gwt.components.client.xml.Document::xmlResolveEntities(Ljava/lang/String;)(att[3] || att[4] || '');
          node.@com.gwt.components.client.xml.Node::setAttribute(Ljava/lang/String;Ljava/lang/String;)(att[1], val);
        }

        //TODO polymorphism here would be nice
        if(parent == xmldoc) {
          xmldoc.@com.gwt.components.client.xml.Document::appendChild(Lcom/gwt/components/client/xml/Node;)(node);
        } else { parent.@com.gwt.components.client.xml.Node::appendChild(Lcom/gwt/components/client/xml/Node;)(node);
        }

        if (!empty) {
          parent = node;
          stack.push(node);
        }
      }

      if (text && parent != root) {
        if(parent == xmldoc) {
          xmldoc.@com.gwt.components.client.xml.Document::appendChild(Lcom/gwt/components/client/xml/Node;)(
              xmldoc.@com.gwt.components.client.xml.Document::createTextNode(Ljava/lang/String;)(text));
        } else {
          parent.@com.gwt.components.client.xml.Node::appendChild(Lcom/gwt/components/client/xml/Node;)(
              xmldoc.@com.gwt.components.client.xml.Document::createTextNode(Ljava/lang/String;)(text));
        }
      }
    }
    return xmldoc;
  }-*/;

  /**
   * @return Returns the childNodes.
   */
  public ArrayList getChildren() {
    return childNodes;
  }
}