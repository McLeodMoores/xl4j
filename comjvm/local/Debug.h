#pragma once
class Debug
{
private:
	Debug ();
	~Debug ();
public:
	static void Debug::odprintf (LPCTSTR sFormat, ...);
};

