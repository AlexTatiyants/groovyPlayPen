class HNItem {
    String itemId
    String itemTitle
    String itemUrl
    Date postedOn
    Date exitedOn    
    Date hitFrontPageOn

    HNItem(id, title, url) {
        itemId = id
        itemTitle = title
        postedOn = new Date()
    }
}

@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
def tagsoupParser = new org.ccil.cowan.tagsoup.Parser()
def slurper = new XmlSlurper(tagsoupParser)

def frontPage = slurper.parse("http://news.ycombinator.com/newest")


def items = frontPage.body.center.table.tr[2].td[0].table

def itemsMap = [:]

def i = 0
def id
while (i < items.tr.size() - 3)  {

    itemId = items.tr[i].td[1].center.a.@id.toString().split('_')[1]
    itemTitle = items.tr[i].td[2].a
    itemUrl = items.tr[i].td[2].a.@href
    if (!itemsMap.containsKey(itemId)) {
        itemsMap[itemId] = new HNItem (itemId, itemTitle, itemUrl)
    }
    i = i + 3
}

itemsMap.each {k, v -> println v.itemTitle}