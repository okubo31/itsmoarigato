package com.itsmoarigato.pages


class MyPage extends Menu{
	static url = '/my'
	static at = {title == 'it\'s more early got you - mypage'}
	static content = {
		"最初のメッセージの更新をクリックする"{
			$('.edit',0).click()
		}
	} 
}
