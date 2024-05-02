package raubach.frickl.next.util;

import java.util.Arrays;

public enum Permission
{
	TAG_ADD(1),
	TAG_DELETE(2),
	IMAGE_UPLOAD(4),
	IMAGE_DELETE(8),
	IMAGE_EDIT(16),
	ALBUM_CREATE(32),
	ALBUM_DELETE(64),
	SETTINGS_CHANGE(128),
	IS_ADMIN(256);

	private final int value;

	Permission(int value)
	{
		this.value = value;
	}

	public int getNumericValue()
	{
		return value;
	}

	public boolean allows(short check)
	{
		return (check & value) == value;
	}

	public static int getAll()
	{
		return Arrays.stream(values()).mapToInt(i -> i.value).reduce(0, (a, b) -> a | b);
	}
}
