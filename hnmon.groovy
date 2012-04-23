class GroovyTimerTask extends TimerTask {
    Closure closure
    void run() {closure()}
}

class TimerMethods {
    static TimerTask runEvery(Timer timer, long delay, long period, Closure codeToRun) {
        TimerTask task = new GroovyTimerTask(closure: codeToRun)
        timer.schedule task, delay, period
        task
    }
}

class HNItem {
    String id
    String title
    String url
    Date postedOn
    Date exitedOn
    Date hitFrontPageOn
    Boolean seenFlag
    
    void calculatePostTime(Integer minutes) {
        postedOn =  new Date(new Date().time - 60000 * minutes)
    }
    
    String toString() {id + '|' + postedOn + '|' + exitedOn + '|' + title + '|' + url + '\n'}
}

// get hacker news html is parse it
@Grab(group='org.ccil.cowan.tagsoup', module='tagsoup', version='1.2' )
def slurper = new XmlSlurper(new org.ccil.cowan.tagsoup.Parser())
def frontPage, items, i = 0, str = "", hnItem, itemsMap = [:]

use (TimerMethods) {
    def timer = new Timer()
    def task = timer.runEvery(100, 30000) {
    
        println 'STARTING RUN AT ' + new Date() + ' ------------'
        
        println 'retrieving items... '
        frontPage = slurper.parse("http://news.ycombinator.com/newest")
        items = frontPage.body.center.table.tr[2].td[0].table

        // reset every current item
        itemsMap.each {k,v -> v.seenFlag = false}
        
        i = 0

        // generate xml representation of news feed
        while (i < items.tr.size() - 3)  {

            // construct new item
            hnItem = new HNItem()
            hnItem.id = items.tr[i].td[1].center.a.@id.toString().split('_')[1]
            hnItem.title = items.tr[i].td[2].a
            hnItem.url = items.tr[i].td[2].a.@href
            hnItem.seenFlag = true

            str = items.tr[i+1].td[1].toString().split(' ')
            
            if (str[5] == 'minute' || str[5] == 'minutes')
                hnItem.calculatePostTime(str[4].toInteger())
            else 
                hnItem.calculatePostTime(60)

            // add to collection if we haven't seen it before                               
            if (!itemsMap.containsKey(hnItem.id)) {                
                itemsMap[hnItem.id] = hnItem
                println 'added new item to collection: ' + itemsMap[hnItem.id].toString()                            
            }
            else {
                itemsMap[hnItem.id].seenFlag = true
            }

            i = i + 3
        }
        
        // for any item not seen, write it to file and delete it from the collection
        iterator = itemsMap.entrySet().iterator()

        f = new File('documents/code/groovyPlayPen/newHNitems.txt')

        while (iterator.hasNext()) {
            hnItem = iterator.next().value
            if (hnItem.seenFlag == false) {
                hnItem.exitedOn = new Date()
                println 'removing item ' + hnItem.id
                f.append(hnItem.toString())
                
                iterator.remove()
            }
        }               
        
         println 'Completed run at: ' + new Date()      
         println ' '
    }
}