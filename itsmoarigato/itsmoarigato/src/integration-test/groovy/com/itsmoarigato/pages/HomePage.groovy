package com.itsmoarigato.pages


/**
 * The home page
 *
 * @author Rob Winch
 */
class HomePage extends Menu {
	static url = '/'
	static at = { assert driver.title == "it's more early got you - main"; true}
	static content = {
		username { $('#un').text() }
		logout(to:LoginPage) { $('input[type=submit]').click() }
		"最初のメッセージの更新をクリックする"{
			waitFor{$('.edit',0) != null}
			$('.edit',0).click()
		}
		"最初のメッセージの削除をクリックする"{
			waitFor{$('.delete',0) != null}
			$('.delete',0).click()
		}
		"最初のメッセージの件名が'delete test'でないべき"{
			waitFor{$('.subject',0) != null}
			$('.subject',0).text() != 'delete test'
		}
		"部長のアイコンをクリックする"{
			waitFor{$('.arigato').size() != 0}
			$('.arigato',0).find('.icon',1).click()
		}
	}
}
