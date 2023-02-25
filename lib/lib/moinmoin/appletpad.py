# -*- coding: iso-8859-1 -*-
"""
    MoinMoin - logout action

    The real logout is done in MoinMoin.request.
    Here is just some stuff to notify the user.

    @copyright: 2005-2006 by Radomirs Cirskis <nad2000@gmail.com>
    @license: GNU GPL, see COPYING for details.
"""

from MoinMoin.Page import Page
import re

def execute(macro, args):
    args = re.split (r'[;\s]+', args)

    try:
        basename = args[0]
    except IndexError:
        basename = "diagram1"

    try:
        plugins = args[1]
    except IndexError:
        plugins = "no"

    try:
        appletMain = args[2]
    except IndexError:
        appletMain = "org.jgraph.pad.jgraphpad.JGraphpad.class"


    pagename = macro.formatter.page.page_name
    request = macro.request
    savelink = Page(request, pagename).url(request) + "?action=AttachFile"
    drawpath = savelink + """&do=get&target=%(basename)s.xml""" % {'basename' : basename}
    pngpath = savelink + """&do=get&target=%(basename)s.png""" % {'basename' : basename}
    fullpng = "http://wiki.visualmodeller.org" + pngpath
    
    readOnly="false"
    if not request.user.may.write(pagename):
        readOnly="true"
    elif not macro.formatter.page.isWritable():
        readOnly="true"
    
    code = """<script>id='%(basename)s'
    basename='%(basename)s'
    xmlDownloadPath='%(drawpath)s'
    xmlUploadLPath='%(savelink)s'
    imageDownloadPath='%(fullpng)s'
    plugins='%(plugins)s'
    appletMain='%(appletMain)s'
    readOnly='%(readOnly)s'
    </script><script type='text/javascript' src='http://wiki.visualmodeller.org/applets/JGraphpadCE/lib/javascript/firepad.js'></script>""" % {
    'pngpath': pngpath, 'drawpath': drawpath, 'fullpng' : fullpng,
    'savelink': savelink, 'basename': basename, 'plugins': plugins, 'appletMain': appletMain, 'readOnly':readOnly}
    return code
    

